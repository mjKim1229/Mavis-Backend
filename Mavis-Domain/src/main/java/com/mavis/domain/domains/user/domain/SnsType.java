package com.mavis.domain.domains.user.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SnsType implements EnumMapperType {
    KAKAO("카카오"),
    NAVER("네이버"),
    MANUAL("일반");

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
