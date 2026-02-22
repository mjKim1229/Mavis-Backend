package com.mavis.admin.domains.notice.dto;

import com.mavis.domain.domains.notice.domain.NoticeCategory;

public record UpdateNoticeRequest(
        NoticeCategory category,
        String title,
        String content
) {
}
