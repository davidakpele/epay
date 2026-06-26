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
public class LiftRestrictionPayload {
    @NotBlank(message = "Lift note is required")
    @Size(max = 500)
    private String liftNote;
 
    public String getLiftNote() { return liftNote; }
    public void setLiftNote(String liftNote) { this.liftNote = liftNote; }
}
