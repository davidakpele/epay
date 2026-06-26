package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSarPayload {
    @NotBlank(message = "SAR reference is required")
    @Size(max = 100)
    private String sarReference;
 
    public String getSarReference() { return sarReference; }
    public void setSarReference(String sarReference) { this.sarReference = sarReference; }
}
