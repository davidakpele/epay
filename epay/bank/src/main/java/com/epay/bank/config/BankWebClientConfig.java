package com.epay.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class BankWebClientConfig {

    @Value("${paystack.api.url:https://api.paystack.co}")
    private String paystackBaseUrl;

    @Value("${paystack.api.key:}")
    private String paystackSecretKey;

    @Bean("paystackWebClient")
    public WebClient paystackWebClient() {
        return WebClient.builder()
                .baseUrl(paystackBaseUrl)
                .defaultHeader("Authorization", "Bearer " + paystackSecretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
