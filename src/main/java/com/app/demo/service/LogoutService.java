package com.app.demo.service;

public interface LogoutService {
    void logout(String authHeader);
    boolean isTokenBlacklisted(String token);
}
