package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageDTO {
    private Long id;
    private String sessionId;
    private Long userId;
    private String role; 
    private String content;
    private LocalDateTime createdAt;
}
