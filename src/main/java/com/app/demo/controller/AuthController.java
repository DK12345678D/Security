package com.app.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.demo.dto.ChangePasswordRequest;
import com.app.demo.dto.LoginRequest;
import com.app.demo.dto.RegisterRequest;
import com.app.demo.dto.TokenRefreshRequest;
import com.app.demo.dto.TokenRefreshResponse;
import com.app.demo.security.JwtUtil;
import com.app.demo.service.AdminService;
import com.app.demo.service.AuthService;
import com.app.demo.service.LogoutServiceImpl;
import com.app.demo.service.RefreshTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AdminService userService;
	private final AuthService authService;
	private final LogoutServiceImpl logoutService;
	private final RefreshTokenService refreshTokenService;
	private final JwtUtil jwtUtil;

	public AuthController(AdminService userService, AuthService authService, 
			LogoutServiceImpl logoutService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
		this.userService = userService;
		this.authService = authService;
		this.logoutService = logoutService;
		this.refreshTokenService = refreshTokenService;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/Admin/SignUp")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		Map<String, Object> response = authService.register(request);
		
		String email = request.getEmail();
		var refreshTokenObj = refreshTokenService.createRefreshToken(email, "ROLE_ADMIN");
		
		Map<String, Object> finalResponse = new java.util.HashMap<>(response);
		finalResponse.put("refreshToken", refreshTokenObj.getToken());
		return ResponseEntity.ok(finalResponse);
	}

	@PostMapping("/Admin/SignIn")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		Map<String, Object> result = authService.login(request);

		String email = null;
		String role = null;
		if (result.get("admin") instanceof Map) {
			Map<?, ?> adminInfo = (Map<?, ?>) result.get("admin");
			email = (String) adminInfo.get("email");
			role = (String) adminInfo.get("role");
		}
		if (email == null) {
			email = request.getIdentifier();
		}
		if (role == null) {
			role = "ROLE_ADMIN";
		}
		var refreshTokenObj = refreshTokenService.createRefreshToken(email, role);
		Map<String, Object> finalResult = new java.util.HashMap<>(result);
		finalResult.put("refreshToken", refreshTokenObj.getToken());

		return ResponseEntity.ok(Map.of("message", "Login successful", "data", finalResult));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@Valid @RequestBody TokenRefreshRequest request) {
		String requestRefreshToken = request.getRefreshToken();
		return refreshTokenService.findByToken(requestRefreshToken)
				.map(refreshTokenService::verifyExpiration)
				.map(token -> {
					String accessToken = jwtUtil.generateToken(token.getUsername(), token.getRole());
					var newRefreshToken = refreshTokenService.createRefreshToken(token.getUsername(), token.getRole());
					return ResponseEntity.ok(new TokenRefreshResponse(accessToken, newRefreshToken.getToken()));
				})
				.orElseThrow(() -> new com.app.demo.exception.UnauthorizedException("Refresh token is not in database!"));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				String email = jwtUtil.extractEmail(token);
				if (email != null) {
					refreshTokenService.deleteByUsername(email);
				}
			} catch (Exception e) {
				
			}
		}
		logoutService.logout(authHeader);
		return ResponseEntity.ok(Map.of("message", "logout successfully"));
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, Authentication auth) {
		if (auth == null || !auth.isAuthenticated()) {
			return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
		}
		authService.changePassword(auth.getPrincipal(), request);
		return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
	}

}