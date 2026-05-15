package com.mavis.infrastructure.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDirectory {
    REVIEW("review"),
    REFUND("refund"),
    INQUIRY("inquiry"),
    PRODUCT("product");

    private final String path;
}
