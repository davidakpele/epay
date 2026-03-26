package com.example.admin_api_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.admin_api_service.Interfaces.IBulkOperationJobService;
import com.example.admin_api_service.Interfaces.IBulkOperationRecordService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.BulkOperationStatus;
import com.example.admin_api_service.enums.BulkOperationType;
import com.example.admin_api_service.enums.BulkRecordStatus;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationJob;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationRecord;
import com.example.admin_api_service.payloads.BulkOperationJobRequest;
import com.example.admin_api_service.payloads.BulkRecordBatchRequest;
import com.example.admin_api_service.payloads.ReasonRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bulk-operations")
public class BulkOperationJobController {

    private final IBulkOperationJobService jobService;
    private final IBulkOperationRecordService recordService;

    public BulkOperationJobController(IBulkOperationJobService jobService,
                                       IBulkOperationRecordService recordService) {
        this.jobService = jobService;
        this.recordService = recordService;
    }

    // ── Job endpoints ──────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<BulkOperationJob>> createJob(
            @Valid @RequestBody BulkOperationJobRequest request,
            @AuthenticationPrincipal String adminId,
            HttpServletRequest httpRequest) {

        BulkOperationJob created = jobService.createJob(
                request.getName(),
                request.getOperationType(),
                request.getSourceReference(),
                request.getParameters(),
                request.getTotalRecords(),
                adminId,
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Bulk operation job created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BulkOperationJob>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BulkOperationStatus status,
            @RequestParam(required = false) BulkOperationType type) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<BulkOperationJob> jobs;

        if (status != null) {
            jobs = jobService.getJobsByStatus(status, pageable);
        } else if (type != null) {
            jobs = jobService.getJobsByType(type, pageable);
        } else {
            jobs = jobService.getAllJobs(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<BulkOperationJob>> getJobById(
            @PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(jobId)));
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<ApiResponse<Page<BulkOperationJob>>> getMyJobs(
            @AuthenticationPrincipal String adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                jobService.getJobsByCreatedBy(
                        adminId, PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @PatchMapping("/{jobId}/approve")
    public ResponseEntity<ApiResponse<BulkOperationJob>> approveJob(
            @PathVariable String jobId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                jobService.approveJob(jobId, adminId), "Job approved"));
    }

    @PatchMapping("/{jobId}/start")
    public ResponseEntity<ApiResponse<BulkOperationJob>> startJob(
            @PathVariable String jobId) {

        return ResponseEntity.ok(ApiResponse.success(
                jobService.startJob(jobId), "Job started"));
    }

    @PatchMapping("/{jobId}/complete")
    public ResponseEntity<ApiResponse<BulkOperationJob>> completeJob(
            @PathVariable String jobId,
            @RequestBody(required = false) Map<String, String> body) {

        String errorReportUrl = body != null ? body.get("errorReportUrl") : null;
        return ResponseEntity.ok(ApiResponse.success(
                jobService.completeJob(jobId, errorReportUrl), "Job completed"));
    }

    @PatchMapping("/{jobId}/fail")
    public ResponseEntity<ApiResponse<BulkOperationJob>> failJob(
            @PathVariable String jobId,
            @Valid @RequestBody ReasonRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                jobService.failJob(jobId, request.getReason()), "Job marked as failed"));
    }

    @PatchMapping("/{jobId}/cancel")
    public ResponseEntity<ApiResponse<BulkOperationJob>> cancelJob(
            @PathVariable String jobId,
            @Valid @RequestBody ReasonRequest request,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                jobService.cancelJob(jobId, adminId, request.getReason()), "Job cancelled"));
    }

    // ── Record endpoints ───────────────────────────────────────────────────────

    @PostMapping("/{jobId}/records/batch")
    public ResponseEntity<ApiResponse<Void>> createRecordsBatch(
            @PathVariable String jobId,
            @Valid @RequestBody BulkRecordBatchRequest request) {

        recordService.createRecordsBatch(
                jobId, request.getTargetIds(), request.getInputPayloads());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Records created"));
    }

    @GetMapping("/{jobId}/records")
    public ResponseEntity<ApiResponse<Page<BulkOperationRecord>>> getRecordsByJob(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) BulkRecordStatus status) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("rowIndex").ascending());
        Page<BulkOperationRecord> records = status != null
                ? recordService.getRecordsByJobAndStatus(jobId, status, pageable)
                : recordService.getRecordsByJob(jobId, pageable);

        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/{jobId}/records/failed")
    public ResponseEntity<ApiResponse<List<BulkOperationRecord>>> getFailedRecords(
            @PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                recordService.getFailedRecordsForJob(jobId)));
    }

    @PatchMapping("/{jobId}/records/{recordId}/retry")
    public ResponseEntity<ApiResponse<BulkOperationRecord>> retryRecord(
            @PathVariable String jobId,
            @PathVariable String recordId) {

        return ResponseEntity.ok(ApiResponse.success(
                recordService.retryRecord(recordId), "Record queued for retry"));
    }
}