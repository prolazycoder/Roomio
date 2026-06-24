package com.hostelapp.core.service;

import com.hostelapp.core.entity.User;
import com.hostelapp.core.dto.RegisterRequest;
import com.hostelapp.core.dto.LoginRequest;
import com.hostelapp.core.dto.AuthResponse;

public interface AuthService {
    User register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse rotateRefreshToken(String token);
}
