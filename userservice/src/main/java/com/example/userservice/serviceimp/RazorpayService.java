package com.example.userservice.serviceimp;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayService {
    @Value("${razorpay.api.key}")
    private String apiKey;
    @Value("${razorpay.api.secret}")
    private String apiSecret;
    private final RazorpayClient razorpayClient;
    private static final String AMT ="amount";
    private static final String CURR ="currency";
    private static final String REC="receipt";

    public Map<String, Object> createOrder(int amountInPaise) throws RazorpayException {

        JSONObject request = new JSONObject();
        request.put(AMT, amountInPaise);
        request.put(CURR, "INR");
        request.put(REC, "crypt_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(request);

        return Map.of(
                "orderId", order.get("id"),
                AMT, order.get(AMT),
                CURR, order.get(CURR),
                REC, order.get(REC)
        );
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {

        JSONObject payload = new JSONObject();
        payload.put("razorpay_order_id", orderId);
        payload.put("razorpay_payment_id", paymentId);
        payload.put("razorpay_signature", signature);

        try {
            return com.razorpay.Utils.verifyPaymentSignature(payload, apiSecret);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
}
