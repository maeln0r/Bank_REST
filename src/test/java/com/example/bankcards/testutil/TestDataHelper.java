package com.example.bankcards.testutil;

import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestDataHelper {
    private final JpaUserRepository users;
    private final PasswordEncoder encoder;

    public TestDataHelper(JpaUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public UserEntity ensureUser(String username, String email, String rawPassword) {
        return users.findByUsernameIgnoreCase(username)
                .orElseGet(() -> users.save(UserEntity.builder()
                        .username(username)
                        .email(email.toLowerCase())
                        .passwordHash(encoder.encode(rawPassword))
                        .enabled(true)
                        .roles(Set.of(RoleType.ROLE_USER))
                        .build()));
    }

    public UserEntity ensureAdmin(String username, String email, String rawPassword) {
        return users.findByUsernameIgnoreCase(username)
                .orElseGet(() -> users.save(UserEntity.builder()
                        .username(username)
                        .email(email.toLowerCase())
                        .passwordHash(encoder.encode(rawPassword))
                        .enabled(true)
                        .roles(Set.of(RoleType.ROLE_ADMIN))
                        .build()));
    }
}