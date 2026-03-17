package com.example.admin_api_service.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import com.example.admin_api_service.dto.EmailAttachment;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private boolean html;
    private List<EmailAttachment> attachments;
    private Map<String, String> headers;
    private String from; 
    private String replyTo;
    private String cc;
    private String bcc;
    private String templateName;
    private Map<String, Object> templateVariables;
    private int priority = 3;
    private boolean trackOpens;
    private boolean trackClicks;
}
