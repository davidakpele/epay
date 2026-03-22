package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.BulkRecordStatus;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationRecord;

public interface IBulkOperationRecordService {
    BulkOperationRecord createRecord(String jobId, int rowIndex, String targetId, String inputPayload);
 
    BulkOperationRecord getRecordById(String recordId);
 
    Page<BulkOperationRecord> getRecordsByJob(String jobId, Pageable pageable);
 
    Page<BulkOperationRecord> getRecordsByJobAndStatus(String jobId, BulkRecordStatus status, Pageable pageable);
 
    List<BulkOperationRecord> getFailedRecordsForJob(String jobId);
 
    BulkOperationRecord markSuccess(String recordId, String resultPayload, String historyReferenceId);
 
    BulkOperationRecord markFailed(String recordId, String errorCode, String errorMessage);
 
    BulkOperationRecord markSkipped(String recordId, String reason);
 
    BulkOperationRecord retryRecord(String recordId);
 
    void createRecordsBatch(String jobId, List<String> targetIds, List<String> inputPayloads);
}
