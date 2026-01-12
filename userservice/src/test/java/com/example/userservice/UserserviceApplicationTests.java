package com.example.userservice;

import com.example.userservice.config.RazorpayConfig;
import com.example.userservice.serviceimp.RazorpayService;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class UserserviceApplicationTests {

    @MockitoBean
    private RazorpayService razorpayService;
    @MockitoBean
    private RazorpayConfig razorpayConfig;
    @MockitoBean
    private RazorpayClient razorpayClient;

    @Test
    void contextLoads() {

    }
}
