package com.example.admin_api_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDeliveryStatus {
    private String emailId;
    private String status; 
    private String message;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private LocalDateTime lastClickedAt;
    private int openCount;
    private int clickCount;
    private Map<String, Object> providerDetails;
    private List<String> failureReasons;
}
