package com.example.bankcards.dto.admin;

import com.example.bankcards.entity.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class UserAdminDtos {
    public record UserResponse(
            UUID id,
            String username,
            String email,
            boolean enabled,
            Set<RoleType> roles,
            OffsetDateTime createdAt
    ) {}

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 100) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password,
            Set<RoleType> roles,
            Boolean enabled
    ) {}

    public record UpdateUserRequest(
            @Size(min = 3, max = 100) String username,
            @Email String email,
            Set<RoleType> roles,
            Boolean enabled
    ) {}

    public record ChangePasswordRequest(
            @NotBlank @Size(min = 6, max = 100) String newPassword
    ) {}
}