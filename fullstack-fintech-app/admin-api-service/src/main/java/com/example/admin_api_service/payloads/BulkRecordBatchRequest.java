package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRecordBatchRequest {
    @NotEmpty(message = "Target IDs cannot be empty")
    private List<String> targetIds;
 
    @NotEmpty(message = "Input payloads cannot be empty")
    private List<String> inputPayloads;
 
    public List<String> getTargetIds() { return targetIds; }
    public void setTargetIds(List<String> targetIds) { this.targetIds = targetIds; }
    public List<String> getInputPayloads() { return inputPayloads; }
    public void setInputPayloads(List<String> inputPayloads) { this.inputPayloads = inputPayloads; }
}
