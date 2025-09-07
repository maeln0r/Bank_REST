package com.example.bankcards.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProps.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProps p) {
        CorsConfiguration c = new CorsConfiguration();
        if (!p.getAllowedOrigins().isEmpty()) {
            c.setAllowedOrigins(p.getAllowedOrigins());
        }
        if (!p.getAllowedOriginPatterns().isEmpty()) {
            c.setAllowedOriginPatterns(p.getAllowedOriginPatterns());
        }
        c.setAllowedMethods(p.getAllowedMethods());
        c.setAllowedHeaders(p.getAllowedHeaders());
        c.setExposedHeaders(p.getExposedHeaders());
        c.setAllowCredentials(Boolean.TRUE.equals(p.getAllowCredentials()));
        c.setMaxAge(p.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        for (String path : p.getPaths()) {
            source.registerCorsConfiguration(path, c);
        }
        return source;
    }
}
