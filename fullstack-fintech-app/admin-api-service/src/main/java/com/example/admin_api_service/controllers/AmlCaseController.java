package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAmlCaseService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseStatus;
import com.example.admin_api_service.models.complianceAndRisk.AmlCase;
import com.example.admin_api_service.payloads.AssignCasePayload;
import com.example.admin_api_service.payloads.EscalateCasePayload;
import com.example.admin_api_service.payloads.FileSarPayload;
import com.example.admin_api_service.payloads.OpenAmlCasePayload;
import com.example.admin_api_service.payloads.ReasonRequest;
import com.example.admin_api_service.payloads.ResolveCasePayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aml-cases")
public class AmlCaseController {

    private final IAmlCaseService amlCaseService;

    public AmlCaseController(IAmlCaseService amlCaseService) {
        this.amlCaseService = amlCaseService;
    }

    
    @PostMapping
    public ResponseEntity<ApiResponse<AmlCase>> openCase(
            @Valid @RequestBody OpenAmlCasePayload payload,
            @AuthenticationPrincipal String adminId) {

        AmlCase amlCase = amlCaseService.openCase(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getCaseType(),
                payload.getPriority(),
                payload.getTitle(),
                payload.getDescription(),
                adminId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(amlCase, "AML case opened"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AmlCase>>> getAllCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AmlCaseStatus status,
            @RequestParam(required = false) AmlCasePriority priority) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<AmlCase> cases;

        if (status != null) {
            cases = amlCaseService.getCasesByStatus(status, pageable);
        } else if (priority != null) {
            cases = amlCaseService.getCasesByPriority(priority, pageable);
        } else {
            cases = amlCaseService.getAllCases(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<AmlCase>> getCaseById(@PathVariable String caseId) {
        return ResponseEntity.ok(ApiResponse.success(amlCaseService.getCaseById(caseId)));
    }

    @GetMapping("/reference/{caseReference}")
    public ResponseEntity<ApiResponse<AmlCase>> getCaseByReference(
            @PathVariable String caseReference) {
        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.getCaseByCaseReference(caseReference)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<AmlCase>>> getCasesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.getCasesByUser(
                        userId, PageRequest.of(page, size, Sort.by("createdOn").descending()))));
    }

    @PatchMapping("/{caseId}/assign")
    public ResponseEntity<ApiResponse<AmlCase>> assignCase(
            @PathVariable String caseId,
            @Valid @RequestBody AssignCasePayload payload) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.assignCase(caseId, payload.getAssignedTo()),
                "Case assigned"));
    }

    @PatchMapping("/{caseId}/escalate")
    public ResponseEntity<ApiResponse<AmlCase>> escalateCase(
            @PathVariable String caseId,
            @Valid @RequestBody EscalateCasePayload payload) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.escalateCase(
                        caseId, payload.getEscalatedTo(), payload.getEscalationReason()),
                "Case escalated"));
    }

    @PatchMapping("/{caseId}/file-sar")
    public ResponseEntity<ApiResponse<AmlCase>> fileSar(
            @PathVariable String caseId,
            @Valid @RequestBody FileSarPayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.fileSar(caseId, payload.getSarReference(), adminId),
                "SAR filed successfully"));
    }

    @PatchMapping("/{caseId}/resolve")
    public ResponseEntity<ApiResponse<AmlCase>> resolveCase(
            @PathVariable String caseId,
            @Valid @RequestBody ResolveCasePayload payload,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.resolveCase(
                        caseId, payload.isSuspiciousActivityConfirmed(),
                        adminId, payload.getClosureNote()),
                "Case resolved"));
    }

    @PatchMapping("/{caseId}/close")
    public ResponseEntity<ApiResponse<AmlCase>> closeCase(
            @PathVariable String caseId,
            @Valid @RequestBody ReasonRequest request,
            @AuthenticationPrincipal String adminId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.closeCase(caseId, adminId, request.getReason()),
                "Case closed"));
    }

    @PatchMapping("/{caseId}/link-freeze/{freezeId}")
    public ResponseEntity<ApiResponse<AmlCase>> linkFreeze(
            @PathVariable String caseId,
            @PathVariable String freezeId) {

        return ResponseEntity.ok(ApiResponse.success(
                amlCaseService.linkFreezeToCase(caseId, freezeId),
                "Freeze linked to case"));
    }
}