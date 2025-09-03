package com.example.bankcards.security;


import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class SecurityProps {
    /**
     * Секрет в Base64 (длина ключа 256+ бит).
     */
    private String secretBase64 = "";
    /**
     * Время жизни access-токена (минут).
     */
    private int accessMinutes = 15;
    /**
     * Время жизни refresh-токена (дней).
     */
    private int refreshDays = 30;


    public String secretBase64() {
        return secretBase64;
    }

    public int accessMinutes() {
        return accessMinutes;
    }

    public int refreshDays() {
        return refreshDays;
    }
}