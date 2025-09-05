package com.example.bankcards.mapper.common;

import org.mapstruct.Named;

import java.time.YearMonth;

public interface CommonMappers {

    @Named("ymToMonth")
    default int ymToMonth(YearMonth ym) {
        return ym.getMonthValue();
    }

    @Named("ymToYear")
    default int ymToYear(YearMonth ym) {
        return ym.getYear();
    }

    @Named("maskLast4")
    default String maskLast4(String last4) {
        return (last4 == null || last4.isBlank()) ? "**** **** **** ****" : "**** **** **** " + last4;
    }
}