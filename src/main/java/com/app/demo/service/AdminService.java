package com.app.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.demo.model.Admin;
import com.app.demo.repo.AdminRepository;

@Service
public class AdminService {
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
		this.adminRepository = adminRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<Admin> getAllAdmins() {
		return adminRepository.findAll();
	}

	public Optional<Admin> getAdminById(Long id) {
		return adminRepository.findById(id);
	}

	public Admin createAdmin(Admin admin) {
		if (adminRepository.findByPhoneNumber(admin.getPhoneNumber()).isPresent()) {
			throw new IllegalArgumentException("Phone number already registered");
		}
		if (admin.getEmail() != null && adminRepository.findByEmailIgnoreCase(admin.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email already registered");
		}
		admin.setPassword(passwordEncoder.encode(admin.getPassword()));
		admin.setRole("ROLE_ADMIN");
		admin.setInactiveUser(0);
		Admin saved = adminRepository.save(admin);
		return saved;
	}

	public Admin updateAdmin(Long id, Admin details) {
		Admin admin = adminRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Admin not found"));

		admin.setFullName(details.getFullName());
		admin.setGender(details.getGender());
		admin.setDob(details.getDob());

		if (details.getEmail() != null && !details.getEmail().equals(admin.getEmail())) {
			admin.setEmail(details.getEmail());
		}

		if (details.getPhoneNumber() != null && !details.getPhoneNumber().equals(admin.getPhoneNumber())) {
			admin.setPhoneNumber(details.getPhoneNumber());
		}

		if (details.getPassword() != null && !details.getPassword().isEmpty()) {
			admin.setPassword(passwordEncoder.encode(details.getPassword()));
		}

		return adminRepository.save(admin);
	}

	public void softDeleteAdmin(Long id) {
		int updatedRows = adminRepository.softDeleteById(id);
		if (updatedRows == 0) {
			throw new RuntimeException("Admin not found with id: " + id);
		}
	}
}
