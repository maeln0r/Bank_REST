package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserSelfDtos.MeResponse;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserSelfService {
    private final JpaUserRepository users;
    private final PasswordEncoder encoder;
    private final CurrentUserService currentUserService;

    public MeResponse me() {
        UserEntity u = currentUserService.getCurrentUser();
        return new MeResponse(u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(), u.getRoles(), u.getCreatedAt());
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        UserEntity u = currentUserService.getCurrentUser();
        if (!encoder.matches(currentPassword, u.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль указан неверно");
        }
        if (encoder.matches(newPassword, u.getPasswordHash())) {
            throw new IllegalArgumentException("Новый пароль не должен совпадать со старым");
        }
        u.setPasswordHash(encoder.encode(newPassword));
    }
}