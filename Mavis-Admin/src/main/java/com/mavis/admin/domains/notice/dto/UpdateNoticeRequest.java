package com.mavis.admin.domains.notice.dto;

public record UpdateNoticeRequest(
        String category,
        String title,
        String content
) {
}
