package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProps {
    private List<String> paths = List.of("/**");
    private List<String> allowedOrigins = List.of();
    private List<String> allowedOriginPatterns = List.of();
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept-Language", "X-Request-Id");
    private List<String> exposedHeaders = List.of("X-Request-Id", "Content-Disposition");
    private Boolean allowCredentials = true;
    private Long maxAge = 3600L;
}
