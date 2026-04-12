package com.mavis.api.notice.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.notice.domain.Notice;
import lombok.Builder;

@Builder
public record NoticeSummaryResponse(
        Long id,
        String category,
        String title,
        String createdAt
) {
    public static NoticeSummaryResponse of(Notice notice) {
        return NoticeSummaryResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .build();
    }
}
