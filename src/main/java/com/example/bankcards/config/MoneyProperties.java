package com.example.bankcards.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app.money")
public class MoneyProperties {

    @Min(0)
    private int scale = 2;

    @NotNull
    private RoundingMode roundingMode = RoundingMode.HALF_UP;

    @NotNull
    @Positive
    private BigDecimal maxAmount = new BigDecimal("1000000000000.00");

    @NotNull
    @Positive
    private BigDecimal maxBalance = new BigDecimal("1000000000000.00");

}