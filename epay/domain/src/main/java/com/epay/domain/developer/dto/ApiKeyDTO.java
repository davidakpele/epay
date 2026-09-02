package com.epay.domain.developer.dto;

import com.epay.domain.developer.enums.ApiKeyStatus;
import com.epay.domain.developer.enums.ApiMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKeyDTO {
    private Long id;
    private ApiMode mode;
    private String publicKey;
    private String secretKey;
    private String secretKeyPrefix;
    private ApiKeyStatus status;
    private long requestCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
