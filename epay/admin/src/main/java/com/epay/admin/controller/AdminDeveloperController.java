package com.epay.admin.controller;

import com.epay.admin.service.AppWebhookService;
import com.epay.admin.service.DeveloperKeyService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.developer.dto.ApiKeyDTO;
import com.epay.domain.developer.dto.DeveloperAppDTO;
import com.epay.domain.developer.dto.WebhookDTO;
import com.epay.domain.developer.entity.WebhookDeliveryLog;
import com.epay.domain.developer.enums.ApiMode;
import com.epay.domain.developer.enums.WebhookEventType;
import com.epay.domain.developer.input.RegisterWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin — Developer Portal", description = "Admin management of developer apps, keys, webhooks, and live approvals")
@RestController
@RequestMapping("/admin/developer")
@RequiredArgsConstructor
public class AdminDeveloperController {

    private final DeveloperKeyService keyService;
    private final AppWebhookService   webhookService;

    @Operation(
        summary     = "List all developer apps",
        description = "Returns every registered developer application across all users."
    )
    @GetMapping("/apps")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<DeveloperAppDTO>>> listAllApps(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null, keyService.listAllApps(pageable)));
    }

    @Operation(
        summary     = "Get a developer app by ID (admin)",
        description = "Returns full app details including masked API keys for any app on the platform."
    )
    @GetMapping("/apps/{appId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> getApp(@PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                keyService.getApp(appId, 0L, true)));
    }

    @Operation(
        summary     = "Approve live mode for an app",
        description = "Grants production API access to an app that has passed live-mode review. SUPER_USER only."
    )
    @PostMapping("/apps/{appId}/live/approve")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> approveLive(
            @PathVariable Long appId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Live mode approved.",
                keyService.approveLiveMode(appId, extractUserId(auth))));
    }

    @Operation(
        summary     = "Rotate an API key (admin)",
        description = "Admin-initiated key rotation. Useful when a key is suspected compromised."
    )
    @PostMapping("/apps/{appId}/keys/{mode}/rotate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<ApiKeyDTO>> adminRotateKey(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        ApiKeyDTO key = keyService.rotateKey(appId, mode, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Key rotated by admin.", key));
    }

    @Operation(
        summary     = "Revoke an API key (admin)",
        description = "Permanently revokes the specified key. Requires a reason for audit."
    )
    @DeleteMapping("/apps/{appId}/keys/{keyId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> adminRevokeKey(
            @PathVariable Long appId, @PathVariable Long keyId,
            @RequestParam(defaultValue = "Revoked by admin") String reason,
            Authentication auth) {
        keyService.revokeKey(keyId, extractUserId(auth), true, reason);
        return ResponseEntity.ok(ApiResponse.success("Key revoked.", null));
    }

    @Operation(
        summary     = "Register a webhook (admin)",
        description = "Admin-initiated webhook registration for any app — used when a developer needs assisted setup."
    )
    @PostMapping("/apps/{appId}/webhooks")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<WebhookDTO>> adminRegisterWebhook(
            @PathVariable Long appId,
            @Valid @RequestBody RegisterWebhookRequest request,
            Authentication auth) {
        WebhookDTO webhook = webhookService.registerWebhook(
                appId, request, extractUserId(auth), true);
        return ResponseEntity.ok(ApiResponse.success(
                "Webhook registered. Secret shown once.", webhook));
    }

    @Operation(
        summary     = "Send a test webhook event (admin)",
        description = "Triggers a test delivery to an app's registered webhook URL for verification."
    )
    @PostMapping("/apps/{appId}/webhooks/{mode}/test")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminTestWebhook(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        Map<String, Object> result = webhookService.sendTestEvent(
                appId, mode, extractUserId(auth), true);
        return ResponseEntity.ok(ApiResponse.success(null, result));
    }

    @Operation(
        summary     = "Delete a webhook (admin)",
        description = "Admin-initiated removal of a webhook for any app."
    )
    @DeleteMapping("/apps/{appId}/webhooks/{mode}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteWebhook(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        webhookService.deleteWebhook(appId, mode, extractUserId(auth), true);
        return ResponseEntity.ok(ApiResponse.success("Webhook deactivated.", null));
    }

    @Operation(
        summary     = "Get all delivery logs for an app (admin)",
        description = "Returns the complete webhook delivery history for any app on the platform."
    )
    @GetMapping("/apps/{appId}/logs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getAppLogs(
            @PathVariable Long appId, @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getAppDeliveryLogs(appId, pageable)));
    }

    @Operation(
        summary     = "Get delivery logs for a webhook (admin)",
        description = "Returns a paginated history of delivery attempts for a specific webhook."
    )
    @GetMapping("/apps/{appId}/webhooks/{webhookId}/logs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getWebhookLogs(
            @PathVariable Long appId, @PathVariable Long webhookId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getDeliveryLogs(webhookId, pageable)));
    }

    @Operation(
        summary     = "List all webhook event types (admin)",
        description = "Returns every event type the platform can deliver."
    )
    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Object>> listEventTypes() {
        var events = java.util.Arrays.stream(WebhookEventType.values())
                .map(e -> Map.of("event", e.name()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(null, events));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null) return 0L;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) return n.longValue();
        }
        return 0L;
    }
}
