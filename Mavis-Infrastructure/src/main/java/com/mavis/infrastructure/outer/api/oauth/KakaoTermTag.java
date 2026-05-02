package com.mavis.infrastructure.outer.api.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KakaoTermTag {
    MARKETING_EMAIL("marketing_email"),
    MARKETING_SMS("marketing_sms");

    private final String tag;
}
