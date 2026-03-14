package com.mavis.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNumberGenerator {
    public static final String DOMAIN_PREFIX = "GARAM";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateOrderId() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);

        long randomNum = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        String randomPart = String.format("%010d", randomNum);
        return DOMAIN_PREFIX + datePart + randomPart;
    }
}
