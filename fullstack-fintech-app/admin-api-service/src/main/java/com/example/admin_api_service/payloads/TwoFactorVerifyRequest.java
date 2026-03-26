package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.TwoFactorMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequest {
    @NotNull(message = "Method is required")
    private TwoFactorMethod method;
 
    @NotBlank(message = "Code is required")
    private String code;
 
    public TwoFactorMethod getMethod() { return method; }
    public void setMethod(TwoFactorMethod method) { this.method = method; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
