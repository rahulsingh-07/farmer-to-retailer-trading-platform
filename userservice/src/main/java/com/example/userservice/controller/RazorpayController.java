package com.example.userservice.controller;

import com.example.userservice.records.ApiResponse;
import com.example.userservice.serviceimp.RazorpayService;
import com.example.userservice.records.VerifyPaymentRequest;
import com.example.userservice.serviceimp.OrderServiceImp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class RazorpayController {

    private final RazorpayService razorpayService;
    private final OrderServiceImp orderServiceImp;

    @PostMapping("/order/{orderId}/create")
    public ResponseEntity<ApiResponse<Map<String,Object>>> createOrder(@PathVariable UUID orderId) {
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Create Payment Order ",
                        orderServiceImp.createPaymentOrder(orderId)
                ));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(@RequestBody VerifyPaymentRequest req) {
        orderServiceImp.verifyAndMarkPaid(req);

        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Paid successfully",
                        null
                ));
    }


}
