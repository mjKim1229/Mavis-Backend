package com.mavis.api.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.notice.domain.Notice;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeSummaryResponse(
        Long id,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime createdAt
) {
    public static NoticeSummaryResponse of(Notice notice) {
        return NoticeSummaryResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
