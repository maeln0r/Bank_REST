package com.example.bankcards.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        String sort,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.getSize(),
                p.getNumber(),
                sortToString(p.getSort()),
                p.isFirst(),
                p.isLast(),
                p.getNumberOfElements(),
                p.isEmpty()
        );
    }

    private static String sortToString(Sort sort) {
        if (sort == null || sort.isUnsorted()) return null;
        return sort.stream()
                .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
                .reduce((a, b) -> a + ";" + b)
                .orElse(null);
    }
}
