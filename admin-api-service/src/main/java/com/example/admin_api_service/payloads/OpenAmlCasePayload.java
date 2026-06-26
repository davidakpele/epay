package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAmlCasePayload {
    @NotNull(message = "User ID is required")
    private Long userId;
 
    private Long walletId;
 
    @NotNull(message = "Case type is required")
    private AmlCaseType caseType;
 
    @NotNull(message = "Priority is required")
    private AmlCasePriority priority;
 
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
 
    @Size(max = 1000)
    private String description;
 
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public AmlCaseType getCaseType() { return caseType; }
    public void setCaseType(AmlCaseType caseType) { this.caseType = caseType; }
    public AmlCasePriority getPriority() { return priority; }
    public void setPriority(AmlCasePriority priority) { this.priority = priority; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
