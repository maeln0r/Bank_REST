package com.example.bankcards.dto.user;

import com.example.bankcards.entity.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class UserSelfDtos {
    public record MeResponse(
            UUID id,
            String username,
            String email,
            boolean enabled,
            Set<RoleType> roles,
            OffsetDateTime createdAt
    ) {
    }

    public record UserChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 6, max = 100) String newPassword
    ) {
    }
}