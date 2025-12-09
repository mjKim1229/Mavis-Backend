package com.mavis.common.util;

import java.security.SecureRandom;

public class RandomAuthCodeUtil {
    public static SecureRandom secureRandom = new SecureRandom();

    private static final int EMAIL_AUTH_NUMBER_LENGTH = 6;

    public static Integer generateRandomIntegerNumber() {
        int upperLimit = (int) Math.pow(10, RandomAuthCodeUtil.EMAIL_AUTH_NUMBER_LENGTH);
        return secureRandom.nextInt(upperLimit);
    }
}
