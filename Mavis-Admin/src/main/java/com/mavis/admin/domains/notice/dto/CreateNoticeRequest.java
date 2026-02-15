package com.mavis.admin.domains.notice.dto;

import com.mavis.domain.domains.notice.domain.Notice;

public record CreateNoticeRequest(
        String category,
        String title,
        String content
) {
    public Notice toEntity() {
        return Notice.builder()
                .category(category)
                .title(title)
                .content(content)
                .build();
    }
}
