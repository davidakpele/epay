package com.epay.blacklist.controller;

import com.epay.blacklist.service.BlacklistService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/blacklist")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class BlacklistAdminController {

    private final BlacklistService blacklistService;

    @PostMapping
    public ResponseEntity<?> add(@RequestBody BlacklistRequest request) {
        blacklistService.addToBlacklist(
                request.getType(), request.getValue(),
                request.getUserId(), request.getReason(),
                request.getAddedBy(), request.getExpiresAt());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/account/{userId}")
    public ResponseEntity<?> checkAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(blacklistService.isAccountBlacklisted(userId));
    }

    @GetMapping("/check/ip/{ip}")
    public ResponseEntity<?> checkIp(@PathVariable String ip) {
        return ResponseEntity.ok(blacklistService.isIpBlacklisted(ip));
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

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
