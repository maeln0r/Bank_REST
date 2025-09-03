package com.example.bankcards.dto;


import jakarta.validation.constraints.NotBlank;


public class AuthDtos {


    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {
    }


    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds
    ) {
    }


    public record RefreshRequest(@NotBlank String refreshToken) {
    }
}
