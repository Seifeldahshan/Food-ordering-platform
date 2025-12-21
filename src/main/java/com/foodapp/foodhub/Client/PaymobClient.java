package com.foodapp.foodhub.Client;


import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymobClient {

    private final PaymobConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public String createAuthToken() {
        String url = config.getBaseUrl() + "/api/auth/tokens";

        Map<String, String> body = new HashMap<>();
        body.put("api_key", config.getApiKey());

        ResponseEntity<Map> resp = restTemplate.postForEntity(url, body, Map.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("Failed to get Paymob auth token");
        }
        Object token = resp.getBody().get("token");
        return token != null ? token.toString() : null;
    }

    public String createPaymobOrder(String authToken, long amountCents) {
        String url = config.getBaseUrl() + "/api/ecommerce/orders";
        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", authToken);
        body.put("delivery_needed", "false");
        body.put("amount_cents", amountCents);
        body.put("currency", "EGP");
        body.put("items", new Object[]{});

        ResponseEntity<Map> resp = restTemplate.postForEntity(url, body, Map.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("Failed to create Paymob order");
        }
        Object id = resp.getBody().get("id");
        if (id == null) throw new RuntimeException("Paymob order response missing id");
        return id.toString();
    }

    public String createPaymentKey(String authToken,
                                   String paymobOrderId,
                                   long amountCents,
                                   Map<String, Object> billingData) {
        String url = config.getBaseUrl() + "/api/acceptance/payment_keys";
        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", authToken);
        body.put("amount_cents", amountCents);
        body.put("expiration", 3600);
        body.put("order_id", paymobOrderId);
        body.put("billing_data", billingData);
        body.put("currency", "EGP");
        body.put("integration_id", config.getIntegrationId());

        ResponseEntity<Map> resp = restTemplate.postForEntity(url, body, Map.class);
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("Failed to create Paymob payment key");
        }
        Object token = resp.getBody().get("token");
        if (token == null) throw new RuntimeException("Payment key response missing token");
        return token.toString();
    }

    public String buildIframeUrl(String paymentToken) {
        return config.getBaseUrl() + "/api/acceptance/iframes/" + config.getIframeId() + "?payment_token=" + paymentToken;
    }
}
