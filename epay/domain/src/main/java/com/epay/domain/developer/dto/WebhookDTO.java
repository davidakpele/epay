package com.epay.domain.developer.dto;

import com.epay.domain.developer.enums.ApiMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookDTO {
    private Long id;
    private Long appId;
    private ApiMode mode;
    private String url;
    private String signingSecret;
    private String subscribedEvents;
    private boolean active;
    private long successCount;
    private long failureCount;
    private LocalDateTime lastDeliveryAt;
    private String lastDeliveryStatus;
    private LocalDateTime createdAt;
}
