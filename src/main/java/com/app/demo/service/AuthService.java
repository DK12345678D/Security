package com.app.demo.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.demo.dto.ChangePasswordRequest;
import com.app.demo.dto.LoginRequest;
import com.app.demo.dto.RegisterRequest;
import com.app.demo.exception.ResourceNotFoundException;
import com.app.demo.model.Admin;
import com.app.demo.repo.AdminRepository;
import com.app.demo.security.JwtUtil;

@Service
public class AuthService {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuditLogService auditLogService;

	public AuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder,
			JwtUtil jwtUtil, AuditLogService auditLogService) {
		this.adminRepository = adminRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.auditLogService = auditLogService;
	}

	public Map<String, Object> register(RegisterRequest request) {
		String phone = request.getPhoneNumber();

		if (!phone.matches("^[0-9]{10}$")) {
			throw new IllegalArgumentException("Phone number must be exactly 10 digits");
		}

		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Passwords do not match");
		}

		adminRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(u -> {
			throw new IllegalArgumentException("Phone already registered");
		});

		if (request.getEmail() != null) {
			adminRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(u -> {
				throw new IllegalArgumentException("Email already registered");
			});
		}

		Admin user = new Admin();
		user.setFullName(request.getFullName());
		user.setPhoneNumber(request.getPhoneNumber());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole("ROLE_ADMIN");

		Admin savedUser = adminRepository.save(user);
		String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

		auditLogService.log(
				savedUser.getId().toString(),
				"ADMIN",
				savedUser.getFullName(),
				"Registration",
				"New Admin '" + savedUser.getFullName() + "' registered",
				"WEB",
				"Admin Registration"
		);

		return Map.of("message", "Admin registered successfully", "adminId", savedUser.getId(), "token", token);
	}

	public Map<String, Object> login(LoginRequest request) {
		String identifier = request.getIdentifier() != null ? request.getIdentifier().trim() : "";
		
		if (identifier.isEmpty()) {
			throw new ResourceNotFoundException("Login identifier (email or phone) is required");
		}
		
		Admin user;
		if (identifier.contains("@")) {
			user = adminRepository.findByEmailIgnoreCase(identifier)
					.orElseThrow(() -> new ResourceNotFoundException("Admin Email ID not found: " + identifier));
		} else {
			user = adminRepository.findByPhoneNumber(identifier)
					.orElseThrow(() -> new ResourceNotFoundException("Admin Phone Number not found: " + identifier));
		}

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ResourceNotFoundException("Invalid password");
		}

		String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

		auditLogService.log(
				user.getId().toString(),
				"ADMIN",
				user.getFullName(),
				"Login",
				"Admin '" + user.getFullName() + "' logged in",
				"WEB",
				"Admin Authentication"
		);

		return Map.of("token", token, "admin",
				Map.of("id", user.getId(), "fullName", user.getFullName(), "email", user.getEmail(), "role", user.getRole()));
	}

	public void changePassword(Object principal, ChangePasswordRequest request) {
		if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
			throw new IllegalArgumentException("New passwords do not match");
		}

		if (principal instanceof Admin) {
			Admin user = (Admin) principal;

			if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
				throw new RuntimeException("Invalid old password");
			}
			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
			adminRepository.save(user);
		} else {
			throw new RuntimeException("Unknown User type");
		}
	}
}