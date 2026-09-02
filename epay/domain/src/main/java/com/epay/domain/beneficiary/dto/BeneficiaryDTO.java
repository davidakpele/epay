package com.epay.domain.beneficiary.dto;

import com.epay.domain.beneficiary.enums.BeneficiaryType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BeneficiaryDTO {

    private Long            id;
    private Long            userId;
    private BeneficiaryType beneficiaryType;
    private String          beneficiaryName;
    private String          currency;
    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String bankName;

    private String recipientUsername;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
