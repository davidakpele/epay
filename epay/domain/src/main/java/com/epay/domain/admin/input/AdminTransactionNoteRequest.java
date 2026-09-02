package com.epay.domain.admin.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminTransactionNoteRequest {

    @NotBlank(message = "Note is required")
    @Size(max = 1000)
    private String note;
}
