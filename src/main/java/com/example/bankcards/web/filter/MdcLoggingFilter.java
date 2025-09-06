package com.example.bankcards.web.filter;

import com.example.bankcards.service.CurrentUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String HDR_REQUEST_ID = "X-Request-Id";

    private final @Nullable CurrentUserService currentUserService;

    public MdcLoggingFilter(@Nullable CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = Optional.ofNullable(request.getHeader(HDR_REQUEST_ID))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());
        String path = Optional.ofNullable(request.getRequestURI()).orElse("/");
        String method = Optional.ofNullable(request.getMethod()).orElse("?");
        String userId = "-";
        try {
            if (currentUserService != null) {
                userId = Optional.ofNullable(currentUserService.getCurrentUserId()).map(UUID::toString).orElse("-");
            }
        } catch (Exception ignored) {
        }

        MDC.put("traceId", traceId);
        MDC.put("userId", userId);
        MDC.put("method", method);
        MDC.put("path", path);
        try {
            response.setHeader(HDR_REQUEST_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}