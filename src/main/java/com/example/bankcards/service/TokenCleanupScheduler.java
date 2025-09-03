package com.example.bankcards.service;

import com.example.bankcards.repository.JpaRefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class TokenCleanupScheduler {
    private final JpaRefreshTokenRepository refreshTokens;

    public TokenCleanupScheduler(JpaRefreshTokenRepository refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpired() {
        refreshTokens.deleteByExpiresAtBefore(OffsetDateTime.now());
    }
}
