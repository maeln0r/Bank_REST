package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;


    @Column(nullable = false, unique = true, length = 100)
    private String username;


    @Column(nullable = false, unique = true, length = 255)
    private String email;


    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;


    @Column(nullable = false)
    private boolean enabled = true;


    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();


    @ElementCollection(targetClass = RoleType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Set<RoleType> roles = new HashSet<>();
}
