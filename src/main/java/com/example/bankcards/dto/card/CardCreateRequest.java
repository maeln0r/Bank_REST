package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.YearMonth;
import java.util.UUID;
import java.math.BigDecimal;

public record CardCreateRequest(
        @NotNull UUID ownerId,
        @NotNull @JsonFormat(pattern = "yyyy-MM") YearMonth expiry,
        @Pattern(regexp = "\\d{4}", message = "last4 must be 4 digits") String last4,
        @PositiveOrZero BigDecimal initialBalance
) {
    @AssertTrue(message = "expiry must be this month or later")
    public boolean isExpiryValid() {
        return expiry != null && !expiry.isBefore(YearMonth.now());
    }
}
