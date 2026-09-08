package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponseDTO {
    private String sessionId;
    private String reply;  
    private String role;  
}
