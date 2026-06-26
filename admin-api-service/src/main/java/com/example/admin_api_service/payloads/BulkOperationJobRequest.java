package com.example.admin_api_service.payloads;

import com.example.admin_api_service.enums.BulkOperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationJobRequest {
    @NotBlank(message = "Job name is required")
    private String name;
 
    @NotNull(message = "Operation type is required")
    private BulkOperationType operationType;
 
    private String sourceReference;
 
    private String parameters;
 
    @Min(value = 1, message = "Total records must be at least 1")
    private int totalRecords;
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BulkOperationType getOperationType() { return operationType; }
    public void setOperationType(BulkOperationType operationType) { this.operationType = operationType; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
}
