package com.mavis.infrastructure.image;

public enum ImageDirectory {
    REVIEW("review"),
    REFUND("refund"),
    INQUIRY("inquiry"),
    PRODUCT("product");

    private final String path;

    ImageDirectory(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
