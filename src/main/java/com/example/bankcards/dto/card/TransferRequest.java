package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID fromCardId,
        @NotNull UUID toCardId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal amount
) {
}