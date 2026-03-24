package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.SanctionListType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionEntryPayload {
    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;
 
    @Size(max = 500)
    private String aliasNames;
 
    @NotNull(message = "List type is required")
    private SanctionListType listType;
 
    @NotBlank(message = "Issuing authority is required")
    @Size(max = 100)
    private String issuingAuthority;
 
    @Size(max = 100)
    private String nationality;
 
    @Size(max = 50)
    private String dateOfBirth;
 
    @Size(max = 100)
    private String nationalId;
 
    @Size(max = 50)
    private String passportNumber;
 
    @Size(max = 500)
    private String address;
 
    @Size(max = 1000)
    private String reason;
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAliasNames() { return aliasNames; }
    public void setAliasNames(String aliasNames) { this.aliasNames = aliasNames; }
    public SanctionListType getListType() { return listType; }
    public void setListType(SanctionListType listType) { this.listType = listType; }
    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
