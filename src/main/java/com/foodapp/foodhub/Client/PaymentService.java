package com.foodapp.foodhub.Client;

import com.foodapp.foodhub.entity.Order;
import com.foodapp.foodhub.entity.PaymentTransaction;
import com.foodapp.foodhub.enums.OrderStatus;
import com.foodapp.foodhub.enums.PaymentMethod;
import com.foodapp.foodhub.enums.PaymentStatus;
import com.foodapp.foodhub.exceptions.OrderNotFoundException;
import com.foodapp.foodhub.repository.OrderRepository;
import com.foodapp.foodhub.repository.TransactionRepository;
import com.foodapp.foodhub.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${paymob.hmac-secret}")
    private String hmacSecret;

    private final PaymobClient paymobClient;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public String startOnlinePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException());//todo : check with ayman adding not found exception to be generic

        if (order.getPaymentMethod() == null || order.getPaymentMethod().name().equalsIgnoreCase(PaymentMethod.CASH.name())) {
            throw new IllegalStateException("Payment method must be Online/Card.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Payment can only be started for PENDING orders.");
        }

        String authToken = paymobClient.createAuthToken();
        long amountCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        String paymobOrderId = paymobClient.createPaymobOrder(authToken, amountCents);

        order.setPaymobOrderId(paymobOrderId);
        orderRepository.save(order);

        Map<String, Object> billing = prepareBillingData(order);

        // 4. Generate Payment Key & Iframe
        String paymentKey = paymobClient.createPaymentKey(authToken, paymobOrderId, amountCents, billing);
        return paymobClient.buildIframeUrl(paymentKey);
    }

    @Transactional
    public void handlePaymobCallback(Map<String, Object> payload) {
        Map<String, Object> obj;
        if (payload.containsKey("obj") && payload.get("obj") instanceof Map) {
            obj = (Map<String, Object>) payload.get("obj");
        } else {
            throw new RuntimeException("Invalid Paymob payload: 'obj' field missing");
        }

        boolean success = parseSuccess(payload);
        if (!success) {
            log.warn("Payment was unsuccessful or cancelled by user.");
            return;
        }

        String paymobOrderId = extractPaymobOrderId(payload);
        if (paymobOrderId == null) {
            throw new RuntimeException("Could not extract paymobOrderId from payload");
        }

        Order order = orderRepository.findByPaymobOrderId(paymobOrderId)
                .orElseThrow(() -> new RuntimeException("No order found for Paymob ID: " + paymobOrderId));

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(Long.parseLong(obj.get("id").toString()));
        transaction.setOrder(order);
        transaction.setPaymobOrderId(paymobOrderId);
        transaction.setAmountCents(Long.parseLong(obj.get("amount_cents").toString()));
        transaction.setSuccess(success);
        transaction.setCurrency(obj.get("currency").toString());
        transaction.setStatus(obj.get("data.message") != null ? obj.get("data.message").toString() : "PROCESSED");
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Order {} already marked as paid.", order.getId());
            return;
        }

        orderService.completeOrderPayment(order.getId());
    }

    private Map<String, Object> prepareBillingData(Order order) {
        Map<String, Object> billing = new HashMap<>();
        var user = order.getUser();
        var addr = order.getOrderAddress();

        billing.put("full_name", user.getFullName() != null ? user.getFullName() : "Customer");
        billing.put("email", user.getEmail() != null ? user.getEmail() : "no-reply@example.com");
        billing.put("phone_number", addr != null ? addr.getPhone() : "0000000000");
        billing.put("city", addr != null ? addr.getArea() : "Cairo");
        billing.put("country", "EG");
        billing.put("building", addr != null ? addr.getBuilding() : "NA");
        billing.put("floor", addr != null ? addr.getFloor() : "NA");
        billing.put("apartment", addr != null ? addr.getApartment() : "NA");
        billing.put("street", addr != null ? addr.getStreet() : "NA");
        billing.put("postal_code", "12345");
        billing.put("state", "NA");
        return billing;
    }

    private String extractPaymobOrderId(Map<String, Object> payload) {
        try {
            if (payload.containsKey("obj")) {
                Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
                if (obj.containsKey("order")) {
                    Object orderPart = obj.get("order");
                    if (orderPart instanceof Map) {
                        return ((Map<?, ?>) orderPart).get("id").toString();
                    }
                    return orderPart.toString();
                }
            }
            return payload.get("order") != null ? payload.get("order").toString() : null;
        } catch (Exception e) {
            log.error("Error parsing Paymob payload", e);
            return null;
        }
    }

    private boolean parseSuccess(Map<String, Object> payload) {
        Object success = payload.get("success");
        if (success == null && payload.get("obj") instanceof Map) {
            success = ((Map<?, ?>) payload.get("obj")).get("success");
        }
        return Boolean.parseBoolean(String.valueOf(success));
    }


    public boolean verifyHmac(Map<String, Object> payload, String hmacFromHeader) {
        try {
            Map<String, Object> obj = (Map<String, Object>) payload.get("obj");

            StringBuilder concatenatedString = new StringBuilder();

            String[] keys = {
                    "amount_cents", "created_at", "currency", "error_occured",
                    "has_parent_transaction", "id", "integration_id", "is_3d_secure",
                    "is_auth", "is_capture", "is_refunded", "is_standalone_payment",
                    "is_voided", "order", "owner", "pending", "source_data.pan",
                    "source_data.sub_type", "source_data.type", "success"
            };

            for (String key : keys) {
                Object value = getValueByDotNotation(obj, key);
                concatenatedString.append(value != null ? value.toString() : "");
            }

            String calculatedHmac = calculateHmacSha512(concatenatedString.toString(), hmacSecret);

            return calculatedHmac.equalsIgnoreCase(hmacFromHeader);

        } catch (Exception e) {
            return false;
        }
    }

    private Object getValueByDotNotation(Map<String, Object> map, String key) {
        if (!key.contains(".")) {
            return map.get(key);
        }
        String[] parts = key.split("\\.");
        Map<String, Object> current = map;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.get(parts[i]);
        }
        return current.get(parts[parts.length - 1]);
    }

    private String calculateHmacSha512(String data, String secret) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
