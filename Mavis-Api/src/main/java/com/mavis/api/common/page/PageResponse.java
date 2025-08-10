package com.mavis.api.common.page;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        long totalPages,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
