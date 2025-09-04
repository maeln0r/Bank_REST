package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID ownerId,
        CardStatus status,
        int expMonth,
        int expYear,
        BigDecimal balance,
        String maskedNumber
) {}