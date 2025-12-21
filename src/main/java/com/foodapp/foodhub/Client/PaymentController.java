package com.foodapp.foodhub.Client;


import com.foodapp.foodhub.repository.OrderRepository;
import com.foodapp.foodhub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;


    @PostMapping("/pay/{orderId}")
    public ResponseEntity<?> startPayment(@PathVariable Long orderId) {
        try {
            String iframeUrl = paymentService.startOnlinePayment(orderId);
            return ResponseEntity.ok(Map.of("payment_url", iframeUrl));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not initialize payment. Please try again later."));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<?> paymobCallback(
            @RequestBody Map<String, Object> payload,
            @RequestParam(name = "hmac") String hmac) {

        if (!paymentService.verifyHmac(payload, hmac)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            paymentService.handlePaymobCallback(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
