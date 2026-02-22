package com.mavis.api.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.notice.domain.Notice;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeSummaryResponse(
        Long id,
        String category,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
    public static NoticeSummaryResponse of(Notice notice) {
        return NoticeSummaryResponse.builder()
                .id(notice.getId())
                .category(notice.getCategory().getTitle())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
