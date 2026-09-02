package com.epay.domain.beneficiary.input;

import com.epay.domain.beneficiary.enums.BeneficiaryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateBeneficiaryRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "beneficiaryType is required")
    private BeneficiaryType beneficiaryType;

    @NotBlank(message = "beneficiaryName is required")
    private String beneficiaryName;

    private String currency = "NGN";

    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String bankName;

    private String recipientUsername;
}
