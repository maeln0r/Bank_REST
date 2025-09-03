package com.example.bankcards.repository;


import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
}