package com.mavis.admin.domains.notice.dto;

import com.mavis.domain.domains.notice.domain.Notice;

public record CreateNoticeRequest(
        String title,
        String content
) {
    public Notice toEntity() {
        return Notice.builder()
                .title(title)
                .content(content)
                .build();
    }
}
