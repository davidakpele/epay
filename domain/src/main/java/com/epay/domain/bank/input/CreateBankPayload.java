package com.epay.domain.bank.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
@Data
public class CreateBankPayload {
 
    @NotBlank(message = "Bank code is required")
    private String bankCode;
 
    @NotBlank(message = "Bank name is required")
    private String bankName;
 
    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;
 
    @NotBlank(message = "Account number is required")
    private String accountNumber;
 
    @NotNull(message = "User Id is required")
    private Long userId;
}
 