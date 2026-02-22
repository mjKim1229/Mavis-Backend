package com.mavis.common.util;

public class PhoneNormalizer {
    public static String normalize(String raw) {
        if (raw == null) return null;

        String cleaned = raw.replaceAll("[^0-9+]", "");

        //Naver
        if (cleaned.startsWith("010") && cleaned.length() == 11) {
            cleaned = "+82" + cleaned.substring(1);
        }

        return cleaned;
    }
}
