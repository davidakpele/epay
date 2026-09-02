package com.epay.domain.admin.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminFlagTransactionRequest {

    private boolean amlFlag;

    @Size(max = 1000)
    private String complianceNote;

    private String disputeStatus;

    @Size(max = 100)
    private String disputeReference;
}
