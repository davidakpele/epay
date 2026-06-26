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
public class UnfreezePayload {
    @NotBlank(message = "Unfreeze note is required")
    @Size(max = 500)
    private String unfreezeNote;
 
    public String getUnfreezeNote() { return unfreezeNote; }
    public void setUnfreezeNote(String unfreezeNote) { this.unfreezeNote = unfreezeNote; }
}