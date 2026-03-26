package com.example.admin_api_service.payloads;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.admin_api_service.enums.IpWhitelistScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpWhitelistRequest {
    @NotBlank(message = "IP address or CIDR is required")
    @Size(max = 50)
    private String ipAddressOrCidr;
 
    @Size(max = 100)
    private String label;
 
    private IpWhitelistScope scope = IpWhitelistScope.INDIVIDUAL;
 
    private LocalDateTime expiresAt;
 
    public String getIpAddressOrCidr() { return ipAddressOrCidr; }
    public void setIpAddressOrCidr(String ipAddressOrCidr) { this.ipAddressOrCidr = ipAddressOrCidr; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public IpWhitelistScope getScope() { return scope; }
    public void setScope(IpWhitelistScope scope) { this.scope = scope; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
