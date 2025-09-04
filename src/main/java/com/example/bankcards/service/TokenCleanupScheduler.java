package com.example.bankcards.service;

import com.example.bankcards.repository.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final JpaRefreshTokenRepository refreshTokens;

    // Каждый час чистим протухшие refresh-токены
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpired() {
        long removed = refreshTokens.deleteByExpiresAtBefore(OffsetDateTime.now());
        if (removed > 0) log.info("Refresh tokens cleanup: removed {} expired tokens", removed);
    }
}