package com.example.bankcards.service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.JpaUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final JpaUserRepository users;

    public CurrentUserService(JpaUserRepository users) {
        this.users = users;
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserEntity getCurrentUser() {
        String username = getCurrentUsername();
        return users.findByUsernameIgnoreCase(username)
                .orElseGet(() -> users.findByEmailIgnoreCase(username)
                        .orElseThrow(() -> new NotFoundException("User not found by principal: %s".formatted(username))));
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        return auth.getName();
    }
}