package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaqDTO {
    private Long id;
    private String category;
    private int sortOrder;
    private String question;
    private String answer;
    private String authorName;
    private boolean active;
    private LocalDateTime createdAt;
}
