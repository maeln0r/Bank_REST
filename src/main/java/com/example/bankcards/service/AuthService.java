package com.example.bankcards.service;

import com.example.bankcards.dto.AuthDtos.LoginRequest;
import com.example.bankcards.dto.AuthDtos.RefreshRequest;
import com.example.bankcards.dto.AuthDtos.TokenResponse;
import com.example.bankcards.entity.RefreshTokenEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaRefreshTokenRepository;
import com.example.bankcards.repository.JpaUserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JpaUserRepository users;
    private final JpaRefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final SecurityProps props;

    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.usernameOrEmail(), req.password()));
        UserEntity u = users.findByUsernameIgnoreCase(req.usernameOrEmail())
                .or(() -> users.findByEmailIgnoreCase(req.usernameOrEmail()))
                .orElseThrow();
        return issueTokens(u);
    }

    public TokenResponse refresh(RefreshRequest req) {
        RefreshTokenEntity rt = refreshTokens.findByToken(req.refreshToken())
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("error.refresh.invalid"));
        rt.setRevoked(true);
        refreshTokens.save(rt);
        return issueTokens(rt.getUser());
    }

    private TokenResponse issueTokens(UserEntity u) {
        String access = jwt.generateAccessToken(u.getUsername(), Map.of("roles", u.getRoles()));
        String refresh = UUID.randomUUID().toString();
        RefreshTokenEntity ent = RefreshTokenEntity.builder()
                .user(u)
                .token(refresh)
                .expiresAt(OffsetDateTime.now().plusDays(props.refreshDays()))
                .build();
        refreshTokens.save(ent);
        return new TokenResponse(access, refresh, "Bearer", props.accessMinutes() * 60L);
    }
}