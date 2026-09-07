package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * Returned to the frontend after each chat turn — contains only the
 * assistant reply so the UI can append it to the conversation.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponseDTO {
    private String sessionId;
    private String reply;      // AI assistant text
    private String role;       // always "assistant"
}
