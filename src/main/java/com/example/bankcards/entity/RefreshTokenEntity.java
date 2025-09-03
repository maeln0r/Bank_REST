package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
public class RefreshTokenEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;


    @Column(nullable = false, unique = true, length = 512)
    private String token;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;


    @Column(nullable = false)
    private OffsetDateTime expiresAt;


    @Column(nullable = false)
    private boolean revoked = false;


    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}