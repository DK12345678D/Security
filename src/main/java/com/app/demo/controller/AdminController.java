package com.app.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.demo.model.Admin;
import com.app.demo.repo.AdminRepository;
import com.app.demo.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {
	private final AdminRepository adminRepository;
	private final AdminService adminService;

	private Admin getAdminOrThrow(Authentication auth) {
		if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Admin)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin Not authenticated");
		}
		return (Admin) auth.getPrincipal();

	}

	@GetMapping("/{id}")
	public ResponseEntity<Admin> getUserById(Authentication auth, @PathVariable Long id) {

		Admin currentUser = getAdminOrThrow(auth);

		if (!currentUser.getId().equals(id) && !"ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not access others profile");
		}

		Admin admin = adminRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

		return ResponseEntity.ok(admin);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Admin> updateUser(Authentication auth, @PathVariable Long id,
			@RequestBody Admin updatedUser) {

		Admin currentUser = getAdminOrThrow(auth);

		if (!currentUser.getId().equals(id) && !"ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your own profile");
		}

		Admin savedUser = adminService.updateAdmin(id, updatedUser);

		return ResponseEntity.ok(savedUser);
	}

}