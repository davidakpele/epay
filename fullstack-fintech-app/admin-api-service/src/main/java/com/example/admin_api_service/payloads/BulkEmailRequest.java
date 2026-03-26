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
    private Map<String, String> personalizations;
    
    @Builder.Default
    private int batchSize = 50; 
    
    @Builder.Default
    private int delayBetweenBatches = 1000;
}
