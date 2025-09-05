package com.example.bankcards.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MoneyProperties.class)
public class MoneyConfig {
}