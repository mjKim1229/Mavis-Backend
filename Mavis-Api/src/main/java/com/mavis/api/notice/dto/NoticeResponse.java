package com.mavis.api.notice.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.notice.domain.Notice;
import lombok.Builder;

@Builder
public record NoticeResponse(
        Long id,
        String title,
        String content,
        String createdAt
) {
    public static NoticeResponse of(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .build();
    }
}
