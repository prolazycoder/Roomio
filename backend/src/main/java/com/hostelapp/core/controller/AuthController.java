package com.hostelapp.core.controller;

import com.hostelapp.core.dto.AuthResponse;
import com.hostelapp.core.dto.LoginRequest;
import com.hostelapp.core.dto.RegisterRequest;
import com.hostelapp.core.dto.RefreshTokenRequest;
import com.hostelapp.core.entity.User;
import com.hostelapp.core.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.rotateRefreshToken(request.getRefreshToken()));
    }
}
