package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record CardCreateRequest(
        @NotNull UUID ownerId,
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth expiry,

        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank
        @Pattern(regexp = "[\\d\\s-]{13,23}", message = "PAN must contain 13–19 digits")
        String pan,

        @PositiveOrZero @Digits(integer = 17, fraction = 2)
        BigDecimal initialBalance
) {
}