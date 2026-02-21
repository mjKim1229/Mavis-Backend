package com.mavis.domain.domains.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    UNKNOWN("알수없음");

    private final String title;

    public static Gender fromCode(String code) {
        if (code == null || code.isBlank()) return UNKNOWN;

        return switch (code.toLowerCase()) {
            case "m", "male" -> MALE;
            case "f", "female" -> FEMALE;
            default -> UNKNOWN;
        };
    }
}