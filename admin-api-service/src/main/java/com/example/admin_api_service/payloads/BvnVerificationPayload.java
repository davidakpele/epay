package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BvnVerificationPayload {
    @NotBlank(message = "BVN is required")
    @Size(min = 11, max = 11, message = "BVN must be exactly 11 digits")
    private String bvn;
 
    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }
}
