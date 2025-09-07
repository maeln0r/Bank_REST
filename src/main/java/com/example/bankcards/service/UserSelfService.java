package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserSelfDtos;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.DomainValidationException;
import com.example.bankcards.mapper.UserSelfMapper;
import com.example.bankcards.repository.JpaUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserSelfService {

    private final CurrentUserService currentUserService;
    private final JpaUserRepository users;
    private final PasswordEncoder encoder;
    private final UserSelfMapper mapper;

    public UserSelfService(CurrentUserService currentUserService,
                           JpaUserRepository users,
                           PasswordEncoder encoder,
                           UserSelfMapper mapper) {
        this.currentUserService = currentUserService;
        this.users = users;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public UserSelfDtos.MeResponse me() {
        UserEntity u = currentUserService.getCurrentUser();
        return mapper.toMeResponse(u);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        UserEntity u = currentUserService.getCurrentUser();
        if (!encoder.matches(currentPassword, u.getPasswordHash())) {
            throw new DomainValidationException("error.password.current_invalid", "currentPassword");
        }
        if (encoder.matches(newPassword, u.getPasswordHash())) {
            throw new DomainValidationException("error.password.same_as_old", "newPassword");
        }
        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);
    }
}