package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID fromCardId,
        @NotNull UUID toCardId,
        @NotNull @Positive BigDecimal amount
) {}