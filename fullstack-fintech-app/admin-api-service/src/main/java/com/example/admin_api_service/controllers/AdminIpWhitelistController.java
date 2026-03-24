package com.example.admin_api_service.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.admin_api_service.Interfaces.IAdminIpWhitelistService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;
import com.example.admin_api_service.payloads.IpWhitelistRequest;
import com.example.admin_api_service.payloads.RevokeIpWhitelistRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ip-whitelist")
public class AdminIpWhitelistController {

    private final IAdminIpWhitelistService ipWhitelistService;

    public AdminIpWhitelistController(IAdminIpWhitelistService ipWhitelistService) {
        this.ipWhitelistService = ipWhitelistService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminIpWhitelist>> addEntry(
            @Valid @RequestBody IpWhitelistRequest request,
            @AuthenticationPrincipal String adminId) {

        AdminIpWhitelist entry = new AdminIpWhitelist();
        entry.setIpAddressOrCidr(request.getIpAddressOrCidr());
        entry.setLabel(request.getLabel());
        entry.setScope(request.getScope());
        entry.setExpiresAt(request.getExpiresAt());

        AdminIpWhitelist created = ipWhitelistService.addToWhitelist(entry, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "IP added to whitelist"));
    }

    @PostMapping("/admin/{adminUserId}")
    public ResponseEntity<ApiResponse<AdminIpWhitelist>> addEntryForAdmin(
            @PathVariable Long adminUserId,
            @Valid @RequestBody IpWhitelistRequest request,
            @AuthenticationPrincipal String adminId) {

        AdminIpWhitelist entry = new AdminIpWhitelist();
        entry.setAdminUserId(adminUserId);
        entry.setIpAddressOrCidr(request.getIpAddressOrCidr());
        entry.setLabel(request.getLabel());
        entry.setScope(request.getScope());
        entry.setExpiresAt(request.getExpiresAt());
        AdminIpWhitelist created = ipWhitelistService.addToWhitelist(entry, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "IP whitelisted for admin"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminIpWhitelist>>> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminIpWhitelist> entries = ipWhitelistService.getAllWhitelistEntries(
                PageRequest.of(page, size, Sort.by("createdOn").descending()));
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/global")
    public ResponseEntity<ApiResponse<List<AdminIpWhitelist>>> getGlobalEntries() {
        return ResponseEntity.ok(ApiResponse.success(
                ipWhitelistService.getGlobalWhitelistEntries()));
    }

    @GetMapping("/admin/{adminUserId}")
    public ResponseEntity<ApiResponse<List<AdminIpWhitelist>>> getEntriesForAdmin(
            @PathVariable String adminUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                ipWhitelistService.getWhitelistEntriesForAdmin(adminUserId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminIpWhitelist>> getEntryById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                ipWhitelistService.getWhitelistEntryById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeEntry(
            @PathVariable String id,
            @Valid @RequestBody RevokeIpWhitelistRequest request,
            @AuthenticationPrincipal String adminId) {

        ipWhitelistService.revokeWhitelistEntry(id, adminId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null, "IP whitelist entry revoked"));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkIpAccess(
            @RequestParam String ipAddress,
            @RequestParam(required = false) String adminUserId) {

        boolean globalAllowed = ipWhitelistService.isGlobalIpAllowed(ipAddress);
        boolean adminAllowed = adminUserId != null
                && ipWhitelistService.isIpAllowed(ipAddress, adminUserId);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "ipAddress", ipAddress,
                "globallyAllowed", globalAllowed,
                "adminAllowed", adminAllowed,
                "allowed", globalAllowed || adminAllowed
        )));
    }
}