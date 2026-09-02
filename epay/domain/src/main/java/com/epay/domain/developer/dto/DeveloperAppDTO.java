package com.epay.domain.developer.dto;

import com.epay.domain.developer.enums.ApiMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperAppDTO {
    private Long id;
    private Long ownerUserId;
    private String appName;
    private String description;
    private String websiteUrl;
    private ApiMode mode;
    private boolean liveApproved;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ApiKeyDTO> apiKeys;
    private List<WebhookDTO> webhooks;
}
