package com.mavis.infrastructure.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDirectory {
    REVIEW("review"),
    REFUND("refund"),
    PRODUCT("product"),
    BANNER("banner");

    private final String path;
}
