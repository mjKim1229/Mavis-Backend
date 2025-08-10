package com.mavis.api.common.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public class SliceUtil {
    public static <T> Slice<T> valueOf(List<T> contents, Pageable pageable) {
        boolean hasNext = hasNext(contents, pageable);
        return new SliceImpl<>(
                hasNext ? getContent(contents, pageable) : contents, pageable, hasNext);
    }

    private static <T> boolean hasNext(List<T> contents, Pageable pageable) {
        return pageable.isPaged() && (contents.size() > pageable.getPageSize());
    }

    private static <T> List<T> getContent(List<T> contents, Pageable pageable) {
        return contents.subList(0, pageable.getPageSize());
    }
}
