package com.example.bankcards.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {
    private PageableUtils() {
    }

    public static Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null) return PageRequest.of(0, 20, defaultSort);
        return pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }
}