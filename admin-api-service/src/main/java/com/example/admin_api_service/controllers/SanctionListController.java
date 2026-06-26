package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.ISanctionListService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.SanctionListStatus;
import com.example.admin_api_service.enums.SanctionListType;
import com.example.admin_api_service.models.complianceAndRisk.SanctionList;
import com.example.admin_api_service.payloads.SanctionEntryPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sanction-list")
public class SanctionListController {

    private final ISanctionListService sanctionListService;

    public SanctionListController(ISanctionListService sanctionListService) {
        this.sanctionListService = sanctionListService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SanctionList>> addEntry(
            @Valid @RequestBody SanctionEntryPayload payload) {

        SanctionList entry = mapToSanctionList(payload);
        SanctionList created = sanctionListService.addEntry(entry);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Sanction entry added"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SanctionList>>> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SanctionListStatus status,
            @RequestParam(required = false) SanctionListType type,
            @RequestParam(required = false) String authority) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
        Page<SanctionList> entries;

        if (status != null) {
            entries = sanctionListService.getEntriesByStatus(status, pageable);
        } else if (type != null) {
            entries = sanctionListService.getEntriesByType(type, pageable);
        } else if (authority != null) {
            entries = sanctionListService.getEntriesByAuthority(authority, pageable);
        } else {
            entries = sanctionListService.getAllEntries(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<ApiResponse<SanctionList>> getEntryById(@PathVariable String entryId) {
        return ResponseEntity.ok(ApiResponse.success(sanctionListService.getEntryById(entryId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SanctionList>>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0.7") double minScore) {

        return ResponseEntity.ok(ApiResponse.success(
                sanctionListService.searchByName(name, minScore)));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getActiveCount() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("activeEntries", sanctionListService.countActiveEntries())));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<ApiResponse<SanctionList>> updateEntry(
            @PathVariable String entryId,
            @Valid @RequestBody SanctionEntryPayload payload) {

        SanctionList entry = mapToSanctionList(payload);
        return ResponseEntity.ok(ApiResponse.success(
                sanctionListService.updateEntry(entryId, entry),
                "Entry updated"));
    }

    @PatchMapping("/{entryId}/delist")
    public ResponseEntity<ApiResponse<Void>> delistEntry(@PathVariable String entryId) {
        sanctionListService.delistEntry(entryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Entry delisted"));
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private SanctionList mapToSanctionList(SanctionEntryPayload p) {
        SanctionList s = new SanctionList();
        s.setName(p.getName());
        s.setAliasNames(p.getAliasNames());
        s.setListType(p.getListType());
        s.setIssuingAuthority(p.getIssuingAuthority());
        s.setNationality(p.getNationality());
        s.setDateOfBirth(p.getDateOfBirth());
        s.setNationalId(p.getNationalId());
        s.setPassportNumber(p.getPassportNumber());
        s.setAddress(p.getAddress());
        s.setReason(p.getReason());
        return s;
    }
}