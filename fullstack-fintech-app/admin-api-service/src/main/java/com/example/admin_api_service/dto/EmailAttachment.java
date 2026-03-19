package com.example.admin_api_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {
    private String filename;
    private String contentType;
    private byte[] content; 
    private long size;
}
