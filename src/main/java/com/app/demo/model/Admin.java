package com.app.demo.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "admin")
public class Admin implements java.security.Principal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String phoneNumber;

	@Column(unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	private String gender;

	private String dob;

	private String role = "ROLE_ADMIN";
	
	@Column(name = "inactive_user", nullable = false)
	private Integer inactiveUser = 0;

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(this.role));
	}

	@Override
	public String getName() {
		return email != null ? email : (phoneNumber != null ? phoneNumber : fullName);
	}

}
