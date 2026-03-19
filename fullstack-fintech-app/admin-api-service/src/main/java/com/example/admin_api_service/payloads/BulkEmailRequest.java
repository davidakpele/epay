package com.example.admin_api_service.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequest {
    private List<String> recipients;
    private String subject;
    private String body;
    private boolean html;
    private Map<String, String> personalizations; // For personalized content
    private int batchSize = 50; // Default batch size
    private int delayBetweenBatches = 1000; // Delay in milliseconds
}
