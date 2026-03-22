package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IApprovalRequestService;
import com.example.admin_api_service.Interfaces.IBulkOperationJobService;
import com.example.admin_api_service.enums.ApprovalTargetType;
import com.example.admin_api_service.enums.ApprovalWorkflowType;
import com.example.admin_api_service.enums.BulkOperationStatus;
import com.example.admin_api_service.enums.BulkOperationType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.ApprovalRequest;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationJob;
import com.example.admin_api_service.repository.BulkOperationJobRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class BulkOperationJobServiceImpl implements IBulkOperationJobService {

    private final BulkOperationJobRepository jobRepository;
    private final IApprovalRequestService approvalRequestService;

    public BulkOperationJobServiceImpl(BulkOperationJobRepository jobRepository, IApprovalRequestService approvalRequestService) {
        this.jobRepository = jobRepository;
        this.approvalRequestService = approvalRequestService;
    }

    @Override
    public BulkOperationJob createJob(String name, BulkOperationType operationType,
                                      String sourceReference, String parameters,
                                      int totalRecords, String createdBy, String ipAddress) {
        BulkOperationJob job = new BulkOperationJob();
        job.setName(name);
        job.setOperationType(operationType);
        job.setSourceReference(sourceReference);
        job.setParameters(parameters);
        job.setTotalRecords(totalRecords);
        job.setCreatedBy(createdBy);
        job.setIpAddress(ipAddress);
        job.setStatus(BulkOperationStatus.PENDING);

        BulkOperationJob saved = jobRepository.save(job);

        // Route through approval workflow
        try {
            ApprovalRequest approvalRequest = approvalRequestService.submitRequest(
                    ApprovalWorkflowType.BULK_DISBURSEMENT,
                    ApprovalTargetType.BULK_JOB,
                    saved.getId(),
                    null,
                    null,
                    parameters,
                    createdBy,
                    "Bulk operation: " + name
            );
            saved.setApprovalRequestId(approvalRequest.getId());
            saved.setStatus(BulkOperationStatus.AWAITING_APPROVAL);
        } catch (ResourceNotFoundException e) {
            // No workflow required — move to APPROVED
            saved.setStatus(BulkOperationStatus.APPROVED);
            saved.setApprovedBy(createdBy);
        }

        return jobRepository.save(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BulkOperationJob getJobById(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkOperationJob", "id", jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationJob> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationJob> getJobsByStatus(BulkOperationStatus status, Pageable pageable) {
        return jobRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationJob> getJobsByType(BulkOperationType type, Pageable pageable) {
        return jobRepository.findAllByOperationType(type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationJob> getJobsByCreatedBy(String adminUserId, Pageable pageable) {
        return jobRepository.findAllByCreatedBy(adminUserId, pageable);
    }

    @Override
    public BulkOperationJob approveJob(String jobId, String approvedBy) {
        BulkOperationJob job = getJobById(jobId);
        if (job.getStatus() != BulkOperationStatus.AWAITING_APPROVAL) {
            throw new ConflictException("Job is not awaiting approval. Current status: " + job.getStatus());
        }
        job.setStatus(BulkOperationStatus.APPROVED);
        job.setApprovedBy(approvedBy);
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public BulkOperationJob startJob(String jobId) {
        BulkOperationJob job = getJobById(jobId);
        if (job.getStatus() != BulkOperationStatus.APPROVED
                && job.getStatus() != BulkOperationStatus.SCHEDULED) {
            throw new ConflictException("Job must be approved or scheduled before it can start. Current status: " + job.getStatus());
        }
        job.setStatus(BulkOperationStatus.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public BulkOperationJob updateProgress(String jobId, boolean success) {
        BulkOperationJob job = getJobById(jobId);
        job.setProcessedRecords(job.getProcessedRecords() + 1);
        if (success) {
            job.setSuccessfulRecords(job.getSuccessfulRecords() + 1);
        } else {
            job.setFailedRecords(job.getFailedRecords() + 1);
        }
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public BulkOperationJob completeJob(String jobId, String errorReportUrl) {
        BulkOperationJob job = getJobById(jobId);
        boolean hasFailures = job.getFailedRecords() > 0;
        job.setStatus(hasFailures
                ? BulkOperationStatus.PARTIALLY_COMPLETED
                : BulkOperationStatus.COMPLETED);
        job.setErrorReportUrl(errorReportUrl);
        job.setCompletedAt(LocalDateTime.now());
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public BulkOperationJob failJob(String jobId, String failureReason) {
        BulkOperationJob job = getJobById(jobId);
        job.setStatus(BulkOperationStatus.FAILED);
        job.setFailureReason(failureReason);
        job.setCompletedAt(LocalDateTime.now());
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public BulkOperationJob cancelJob(String jobId, String cancelledBy, String cancellationReason) {
        BulkOperationJob job = getJobById(jobId);
        if (job.getStatus() == BulkOperationStatus.COMPLETED
                || job.getStatus() == BulkOperationStatus.FAILED) {
            throw new ConflictException("Cannot cancel a job that is already " + job.getStatus());
        }
        job.setStatus(BulkOperationStatus.CANCELLED);
        job.setCancelledBy(cancelledBy);
        job.setCancellationReason(cancellationReason);
        job.setUpdatedOn(LocalDateTime.now());
        return jobRepository.save(job);
    }
}
