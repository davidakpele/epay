package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.TwoFactorMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorDisableRequest {
    @NotNull
    private TwoFactorMethod method;
 
    public TwoFactorMethod getMethod() { return method; }
    public void setMethod(TwoFactorMethod method) { this.method = method; }
}
