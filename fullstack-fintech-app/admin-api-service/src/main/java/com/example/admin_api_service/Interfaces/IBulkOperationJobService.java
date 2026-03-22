package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.BulkOperationStatus;
import com.example.admin_api_service.enums.BulkOperationType;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBulkOperationJobService {
    BulkOperationJob createJob(String name, BulkOperationType operationType,
                               String sourceReference, String parameters,
                               int totalRecords, String createdBy, String ipAddress);
 
    BulkOperationJob getJobById(String jobId);
 
    Page<BulkOperationJob> getAllJobs(Pageable pageable);
 
    Page<BulkOperationJob> getJobsByStatus(BulkOperationStatus status, Pageable pageable);
 
    Page<BulkOperationJob> getJobsByType(BulkOperationType type, Pageable pageable);
 
    Page<BulkOperationJob> getJobsByCreatedBy(String adminUserId, Pageable pageable);
 
    BulkOperationJob approveJob(String jobId, String approvedBy);
 
    BulkOperationJob startJob(String jobId);
 
    // Incremental progress update called as each record is processed
    BulkOperationJob updateProgress(String jobId, boolean success);
 
    BulkOperationJob completeJob(String jobId, String errorReportUrl);
 
    BulkOperationJob failJob(String jobId, String failureReason);
 
    BulkOperationJob cancelJob(String jobId, String cancelledBy, String cancellationReason);
}
