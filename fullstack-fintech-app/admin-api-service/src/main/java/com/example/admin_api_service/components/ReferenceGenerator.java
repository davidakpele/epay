package com.example.admin_api_service.components;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ReferenceGenerator {
    public String generateLiquidityRef() {
        return "LIQ-" + System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}