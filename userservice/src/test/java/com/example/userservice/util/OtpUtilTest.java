package com.example.userservice.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpUtilTest {
    @Test
    void generateOtp_shouldReturn6DigitNumericString() {
        String otp = OtpUtil.generateOtp();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        int value = Integer.parseInt(otp);
        assertTrue(value >= 100000 && value <= 999999);
    }

    @RepeatedTest(5)
    void generateOtp_shouldGenerateDifferentValues() {
        String otp1 = OtpUtil.generateOtp();
        String otp2 = OtpUtil.generateOtp();

        assertNotEquals(otp1, otp2);
    }
}
