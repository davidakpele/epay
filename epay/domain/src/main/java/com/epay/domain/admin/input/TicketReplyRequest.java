package com.epay.domain.admin.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketReplyRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private boolean internal = false;
}
