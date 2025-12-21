package com.foodapp.foodhub.Client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "paymob.api")
public class PaymobConfig {
    private String baseUrl;
    private String apiKey;
    private Long integrationId;
    private Long iframeId;

}
