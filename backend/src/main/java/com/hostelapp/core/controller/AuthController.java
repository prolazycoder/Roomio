package com.hostelapp.core.controller;

import com.hostelapp.core.dto.AuthResponse;
import com.hostelapp.core.dto.LoginRequest;
import com.hostelapp.core.dto.RegisterRequest;
import com.hostelapp.core.dto.RefreshTokenRequest;
import com.hostelapp.core.entity.User;
import com.hostelapp.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and token refresh endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account within a workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate email")
    })
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user credentials and returns JWT access + refresh tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens issued"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.rotateRefreshToken(request.getRefreshToken()));
    }
}
