package com.mavis.admin.domains.notice.dto;

import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.domains.notice.domain.NoticeCategory;

public record CreateNoticeRequest(
        NoticeCategory category,
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
