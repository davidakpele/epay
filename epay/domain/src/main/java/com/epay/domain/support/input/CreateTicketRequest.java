package com.epay.domain.support.input;

import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "subject is required")
    @Size(max = 200)
    private String subject;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "category is required")
    private TicketCategory category;

    private TicketPriority priority = TicketPriority.MEDIUM;

    private String relatedTransactionId;
}
