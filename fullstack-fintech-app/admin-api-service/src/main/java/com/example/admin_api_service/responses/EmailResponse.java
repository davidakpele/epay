package com.example.admin_api_service.responses;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private boolean success;
    private String message;
    private String emailId;
    private String providerMessageId;
    private long timestamp;
    private Map<String, Object> metadata;
    
    // Convenience methods
    public static EmailResponse success(String emailId, String providerMessageId) {
        return EmailResponse.builder()
                .success(true)
                .message("Email sent successfully")
                .emailId(emailId)
                .providerMessageId(providerMessageId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static EmailResponse failure(String errorMessage) {
        return EmailResponse.builder()
                .success(false)
                .message(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
