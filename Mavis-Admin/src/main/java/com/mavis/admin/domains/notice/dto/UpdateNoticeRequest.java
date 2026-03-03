package com.mavis.admin.domains.notice.dto;

public record UpdateNoticeRequest(
        String title,
        String content
) {
}
