package com.mavis.common.consts;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavisStatic {
    public static final String BEARER = "Bearer ";
    public static final String TOKEN_ISSUER = "Mavis";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String TOKEN_TYPE = "type";

    public static final int MILLI_TO_SECOND = 1000;
    public static final String AUTH_HEADER = "Authorization";
}
