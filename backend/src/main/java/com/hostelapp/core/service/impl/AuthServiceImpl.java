package com.hostelapp.core.service.impl;

import com.hostelapp.core.dto.AuthResponse;
import com.hostelapp.core.dto.LoginRequest;
import com.hostelapp.core.dto.RegisterRequest;
import com.hostelapp.core.entity.Role;
import com.hostelapp.core.entity.User;
import com.hostelapp.core.entity.RefreshToken;
import com.hostelapp.core.config.JwtUtil;
import com.hostelapp.core.config.TenantContext;
import com.hostelapp.core.repository.RefreshTokenRepository;
import com.hostelapp.core.repository.UserRepository;
import com.hostelapp.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setGovIdType(request.getGovIdType());
        user.setGovIdNumberMasked(maskGovId(request.getGovIdNumber()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        TenantContext.setCurrentTenant(request.getWorkspaceId());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String jwtToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse rotateRefreshToken(String token) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (oldToken.isRevoked() || oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByUserId(oldToken.getUser().getId());
            throw new SecurityException("Refresh token is expired or has been reused!");
        }

        TenantContext.setCurrentTenant(oldToken.getUser().getWorkspaceId());

        User user = oldToken.getUser();
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        String jwtToken = jwtUtil.generateToken(user);
        RefreshToken newToken = createRefreshToken(user);

        return new AuthResponse(jwtToken, newToken.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    private String maskGovId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return null;
        }
        String clean = rawId.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        return "*".repeat(clean.length() - 4) + clean.substring(clean.length() - 4);
    }
}
