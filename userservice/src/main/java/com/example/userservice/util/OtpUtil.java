package com.example.userservice.util;

import java.util.concurrent.ThreadLocalRandom;

public class OtpUtil {
    private OtpUtil() {}
    public static String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );
    }
}
