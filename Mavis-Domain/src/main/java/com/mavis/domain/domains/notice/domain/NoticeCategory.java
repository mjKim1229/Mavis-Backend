package com.mavis.domain.domains.notice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeCategory {
    DELIVERY("배송 안내");

    private final String title;
}
