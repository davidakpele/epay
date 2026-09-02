package com.epay.domain.admin.input;

import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTicketRequest {

    private TicketStatus   status;
    private TicketPriority priority;
    private TicketCategory category;

    @Size(max = 200)
    private String subject;

    private Long assignedTo;

    @Size(max = 2000)
    private String internalNote;
}
