package com.epay.domain.support.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "sessionId is required")
    @Size(max = 64)
    private String sessionId;

    private Long userId;

    @NotBlank(message = "message is required")
    @Size(max = 4000, message = "message must not exceed 4000 characters")
    private String message;
}
