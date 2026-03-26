package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IComplianceReportService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.ComplianceReportStatus;
import com.example.admin_api_service.enums.ComplianceReportType;
import com.example.admin_api_service.models.complianceAndRisk.ComplianceReport;
import com.example.admin_api_service.payloads.CreateComplianceReportPayload;
import com.example.admin_api_service.payloads.RejectComplianceReportPayload;
import com.example.admin_api_service.payloads.ReviewComplianceReportPayload;
import com.example.admin_api_service.payloads.SubmitComplianceReportPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compliance-reports")
public class ComplianceReportController {

    private final IComplianceReportService reportService;

    public ComplianceReportController(IComplianceReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComplianceReport>> createReport(
            @Valid @RequestBody CreateComplianceReportPayload payload,
            @AuthenticationPrincipal String adminId) {

        ComplianceReport report = reportService.createReport(
                payload.getReportType(),
                payload.getTitle(),
                payload.getPeriodStart(),
                payload.getPeriodEnd(),
                payload.getRegulatoryBody(),
                payload.getFormat(),
                payload.getDueDate(),
                adminId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(report, "Compliance report created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComplianceReport>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ComplianceReportStatus status,
            @RequestParam(required = false) ComplianceReportType type) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<ComplianceReport> reports;

        if (status != null) {
            reports = reportService.getReportsByStatus(status, pageable);
        } else if (type != null) {
            reports = reportService.getReportsByType(type, pageable);
        } else {
            reports = reportService.getAllReports(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ComplianceReport>> getReportById(
            @PathVariable String reportId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportById(reportId)));
    }

    @GetMapping("/reference/{reportReference}")
    public ResponseEntity<ApiResponse<ComplianceReport>> getReportByReference(
            @PathVariable String reportReference) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getReportByReference(reportReference)));
    }

    @PatchMapping("/{reportId}/generate")
    public ResponseEntity<ApiResponse<ComplianceReport>> generateReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.generateReport(reportId, adminId),
                "Report generation started"));
    }

    @PatchMapping("/{reportId}/submit-for-review")
    public ResponseEntity<ApiResponse<ComplianceReport>> submitForReview(
            @PathVariable String reportId,
            @Valid @RequestBody ReviewComplianceReportPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.submitForReview(reportId, adminId, payload.getReviewNote()),
                "Report submitted for review"));
    }

    @PatchMapping("/{reportId}/approve")
    public ResponseEntity<ApiResponse<ComplianceReport>> approveReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.approveReport(reportId, adminId),
                "Report approved"));
    }

    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<ComplianceReport>> rejectReport(
            @PathVariable String reportId,
            @Valid @RequestBody RejectComplianceReportPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.rejectReport(reportId, adminId, payload.getRejectionReason()),
                "Report rejected"));
    }

    @PatchMapping("/{reportId}/submit")
    public ResponseEntity<ApiResponse<ComplianceReport>> submitReport(
            @PathVariable String reportId,
            @Valid @RequestBody SubmitComplianceReportPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.submitReport(reportId, adminId, payload.getSubmissionReference()),
                "Report submitted to regulatory body"));
    }

    @PatchMapping("/{reportId}/archive")
    public ResponseEntity<ApiResponse<ComplianceReport>> archiveReport(
            @PathVariable String reportId) {

        return ResponseEntity.ok(ApiResponse.success(
                reportService.archiveReport(reportId),
                "Report archived"));
    }
}