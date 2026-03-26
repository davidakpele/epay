package com.example.admin_api_service.payloads;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderStepsRequest {
    @NotEmpty(message = "Ordered step IDs cannot be empty")
    private List<String> orderedStepIds;
 
    public List<String> getOrderedStepIds() { return orderedStepIds; }
    public void setOrderedStepIds(List<String> orderedStepIds) { this.orderedStepIds = orderedStepIds; }
}
