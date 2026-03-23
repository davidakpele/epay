package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.KycDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentUploadPayload {
    @NotNull(message = "Document type is required")
    private KycDocumentType documentType;
 
    @Size(max = 100)
    private String documentNumber;
 
    @Size(max = 50)
    private String issuingCountry;
 
    @NotBlank(message = "Front file URL is required")
    @Size(max = 500)
    private String frontFileUrl;
 
    @Size(max = 500)
    private String backFileUrl;
 
    @Size(max = 500)
    private String selfieFileUrl;
 
    @Size(max = 50)
    private String fileMimeType;
 
    private Long fileSizeBytes;
 
    public KycDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(KycDocumentType documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getIssuingCountry() { return issuingCountry; }
    public void setIssuingCountry(String issuingCountry) { this.issuingCountry = issuingCountry; }
    public String getFrontFileUrl() { return frontFileUrl; }
    public void setFrontFileUrl(String frontFileUrl) { this.frontFileUrl = frontFileUrl; }
    public String getBackFileUrl() { return backFileUrl; }
    public void setBackFileUrl(String backFileUrl) { this.backFileUrl = backFileUrl; }
    public String getSelfieFileUrl() { return selfieFileUrl; }
    public void setSelfieFileUrl(String selfieFileUrl) { this.selfieFileUrl = selfieFileUrl; }
    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
}
