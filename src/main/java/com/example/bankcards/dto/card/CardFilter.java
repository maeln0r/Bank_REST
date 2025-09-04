package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;

public record CardFilter(
        CardStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo
) {}