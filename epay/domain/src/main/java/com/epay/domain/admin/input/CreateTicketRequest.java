package com.epay.domain.admin.input;

import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Subject is required")
    @Size(max = 200)
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private TicketCategory category;

    private TicketPriority priority = TicketPriority.MEDIUM;
    private String relatedTransactionId;
}
