package com.app.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class LoginRequest {
	@JsonAlias("email")
	private String identifier; // email or phone number
	private String password;
}