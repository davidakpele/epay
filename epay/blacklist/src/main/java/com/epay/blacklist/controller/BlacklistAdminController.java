package com.epay.blacklist.controller;

import com.epay.blacklist.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Admin — Blacklist", description = "Manage IP address and account blacklist entries")
@RestController
@RequestMapping("/admin/blacklist")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class BlacklistAdminController {

    private final BlacklistService blacklistService;

    @Operation(
        summary     = "Add an entry to the blacklist",
        description = "Blocks an IP address or user account. Type must be 'IP' or 'ACCOUNT'. An optional expiry can be set."
    )
    @PostMapping
    public ResponseEntity<?> add(@RequestBody BlacklistRequest request) {
        blacklistService.addToBlacklist(
                request.getType(), request.getValue(),
                request.getUserId(), request.getReason(),
                request.getAddedBy(), request.getExpiresAt());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary     = "Remove a blacklist entry",
        description = "Deletes or deactivates the blacklist entry with the given ID."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary     = "Check if a user account is blacklisted",
        description = "Returns true if the account associated with the given userId is currently on the blacklist."
    )
    @GetMapping("/check/account/{userId}")
    public ResponseEntity<?> checkAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(blacklistService.isAccountBlacklisted(userId));
    }

    @Operation(
        summary     = "Check if an IP address is blacklisted",
        description = "Returns true if the given IP address is currently on the blacklist."
    )
    @GetMapping("/check/ip/{ip}")
    public ResponseEntity<?> checkIp(@PathVariable String ip) {
        return ResponseEntity.ok(blacklistService.isIpBlacklisted(ip));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlacklistRequest {
        private String        type;
        private String        value;
        private Long          userId;
        private String        reason;
        private String        addedBy;
        private LocalDateTime expiresAt;
    }
}
