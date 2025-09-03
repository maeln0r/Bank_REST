package com.example.bankcards.repository;


import com.example.bankcards.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);

    long deleteByUser_Id(UUID userId);

    long deleteByExpiresAtBefore(OffsetDateTime cutoff);
}