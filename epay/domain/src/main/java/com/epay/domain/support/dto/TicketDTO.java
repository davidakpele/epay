package com.epay.domain.support.dto;

import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDTO {

    private Long id;
    private String ticketReference;
    private Long userId;
    private String userEmail;
    private String userUsername;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketCategory category;
    private Long assignedTo;
    private String assignedToName;
    private String relatedTransactionId;
    private String internalNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime escalatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyDTO> replies;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReplyDTO {
        private Long id;
        private Long authorId;
        private String authorName;
        private boolean staffReply;
        private boolean internal;
        private String message;
        private LocalDateTime createdAt;
    }
}
