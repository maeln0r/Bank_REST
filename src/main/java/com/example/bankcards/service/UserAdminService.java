package com.example.bankcards.service;

import com.example.bankcards.dto.admin.UserAdminDtos.CreateUserRequest;
import com.example.bankcards.dto.admin.UserAdminDtos.UpdateUserRequest;
import com.example.bankcards.dto.admin.UserAdminDtos.UserResponse;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAdminService {
    private final JpaUserRepository users;
    private final PasswordEncoder encoder;

    public Page<UserResponse> list(Pageable pageable, String query) {
        return users.findAll(pageable).map(this::toDto);
    }

    public UserResponse get(UUID id) {
        return toDto(require(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        String email = req.email().toLowerCase(Locale.ROOT);
        if (users.existsByUsername(req.username()))
            throw new IllegalArgumentException("Имя пользователя %s уже используется".formatted(req.username()));
        if (users.existsByEmail(email))
            throw new IllegalArgumentException("Email пользователя %s уже используется".formatted(email));

        UserEntity u = UserEntity.builder()
                .username(req.username())
                .email(email)
                .passwordHash(encoder.encode(req.password()))
                .enabled(Boolean.TRUE.equals(req.enabled()))
                .roles(req.roles() == null || req.roles().isEmpty() ? Set.of(RoleType.ROLE_USER) : req.roles())
                .build();

        users.save(u);
        return toDto(u);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest req) {
        UserEntity u = require(id);

        if (req.username() != null && !Objects.equals(req.username(), u.getUsername())) {
            if (users.existsByUsername(req.username()))
                throw new IllegalArgumentException("Имя пользователя %s уже используется".formatted(req.username()));
            u.setUsername(req.username());
        }
        if (req.email() != null && !Objects.equals(req.email(), u.getEmail())) {
            String email = req.email().toLowerCase(Locale.ROOT);
            if (users.existsByEmail(email))
                throw new IllegalArgumentException("Email пользователя %s уже используется".formatted(email));
            u.setEmail(email);
        }
        if (req.enabled() != null) u.setEnabled(req.enabled());
        if (req.roles() != null && !req.roles().isEmpty()) u.setRoles(req.roles());

        return toDto(u);
    }

    @Transactional
    public void changePassword(UUID id, String newPassword) {
        UserEntity u = require(id);
        u.setPasswordHash(encoder.encode(newPassword));
    }

    @Transactional
    public void delete(UUID id) {
        users.deleteById(id);
    }

    private UserEntity require(UUID id) {
        return users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь %s не найден".formatted(id)));
    }

    private UserResponse toDto(UserEntity u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(), u.getRoles(), u.getCreatedAt());
    }
}
