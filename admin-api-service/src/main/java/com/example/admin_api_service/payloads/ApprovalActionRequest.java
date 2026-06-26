package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalActionRequest {
    @Size(max = 500)
    private String note;
 
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
