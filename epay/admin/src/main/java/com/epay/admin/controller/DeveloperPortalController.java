package com.epay.admin.controller;

import com.epay.admin.service.AppWebhookService;
import com.epay.admin.service.DeveloperKeyService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.developer.dto.ApiKeyDTO;
import com.epay.domain.developer.dto.DeveloperAppDTO;
import com.epay.domain.developer.dto.WebhookDTO;
import com.epay.domain.developer.entity.WebhookDeliveryLog;
import com.epay.domain.developer.enums.ApiMode;
import com.epay.domain.developer.input.CreateAppRequest;
import com.epay.domain.developer.input.RegisterWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Developer Portal", description = "Manage developer apps, API keys, webhooks, and live access requests")
@RestController
@RequestMapping("/developer")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class DeveloperPortalController {

    private final DeveloperKeyService keyService;
    private final AppWebhookService   webhookService;

    @Operation(
        summary     = "Create a developer app",
        description = "Registers a new developer application. Returns sandbox API keys — copy the secret key immediately, it will not be shown again."
    )
    @PostMapping("/apps")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> createApp(
            @Valid @RequestBody CreateAppRequest request, Authentication auth) {
        DeveloperAppDTO app = keyService.createApp(request, extractUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "App created. Copy your secret keys now — they will not be shown again.", app));
    }

    @Operation(
        summary     = "List my developer apps",
        description = "Returns all apps owned by the authenticated user."
    )
    @GetMapping("/apps")
    public ResponseEntity<ApiResponse<List<DeveloperAppDTO>>> listMyApps(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                keyService.getMyApps(extractUserId(auth))));
    }

    @Operation(
        summary     = "Get a developer app by ID",
        description = "Returns configuration details for the specified app. Only the app owner can view it."
    )
    @GetMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> getApp(
            @PathVariable Long appId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                keyService.getApp(appId, extractUserId(auth), false)));
    }

    @Operation(
        summary     = "Update a developer app",
        description = "Updates the app name, description, or allowed redirect URLs."
    )
    @PutMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> updateApp(
            @PathVariable Long appId,
            @Valid @RequestBody CreateAppRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("App updated.",
                keyService.updateApp(appId, request, extractUserId(auth))));
    }

    @Operation(
        summary     = "Delete a developer app",
        description = "Permanently removes an app and revokes all associated API keys."
    )
    @DeleteMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<Void>> deleteApp(
            @PathVariable Long appId, Authentication auth) {
        keyService.deleteApp(appId, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("App deleted.", null));
    }

    @Operation(
        summary     = "Rotate an API key",
        description = "Generates a new API key for the specified mode (SANDBOX or LIVE) and invalidates the previous one. Copy the new secret immediately — it will not be shown again."
    )
    @PostMapping("/apps/{appId}/keys/{mode}/rotate")
    public ResponseEntity<ApiResponse<ApiKeyDTO>> rotateKey(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        ApiKeyDTO newKey = keyService.rotateKey(appId, mode, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success(
                "Key rotated. Copy your new secret key — it will not be shown again.", newKey));
    }

    @Operation(
        summary     = "Revoke an API key",
        description = "Permanently revokes the specified API key. Any integrations using it will stop working immediately."
    )
    @DeleteMapping("/apps/{appId}/keys/{keyId}")
    public ResponseEntity<ApiResponse<Void>> revokeKey(
            @PathVariable Long appId, @PathVariable Long keyId,
            @RequestParam(defaultValue = "Revoked by developer") String reason,
            Authentication auth) {
        keyService.revokeKey(keyId, extractUserId(auth), false, reason);
        return ResponseEntity.ok(ApiResponse.success("API key revoked.", null));
    }

    @Operation(
        summary     = "Register a webhook",
        description = "Sets a callback URL for the specified mode (SANDBOX or LIVE). The signing secret is shown only once — store it securely."
    )
    @PostMapping("/apps/{appId}/webhooks")
    public ResponseEntity<ApiResponse<WebhookDTO>> registerWebhook(
            @PathVariable Long appId,
            @Valid @RequestBody RegisterWebhookRequest request,
            Authentication auth) {
        WebhookDTO webhook = webhookService.registerWebhook(
                appId, request, extractUserId(auth), false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Webhook registered. Copy your signing secret — it will not be shown again.",
                        webhook));
    }

    @Operation(
        summary     = "Get webhook configuration",
        description = "Returns the currently registered webhook URL and settings for the specified mode."
    )
    @GetMapping("/apps/{appId}/webhooks/{mode}")
    public ResponseEntity<ApiResponse<WebhookDTO>> getWebhook(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getWebhook(appId, mode, extractUserId(auth), false)));
    }

    @Operation(
        summary     = "Delete a webhook",
        description = "Removes the callback URL for the specified mode. Events will no longer be delivered."
    )
    @DeleteMapping("/apps/{appId}/webhooks/{mode}")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        webhookService.deleteWebhook(appId, mode, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success("Webhook deactivated.", null));
    }

    @Operation(
        summary     = "Send a test webhook event",
        description = "Delivers a sample event payload to the registered webhook URL and returns the delivery result."
    )
    @PostMapping("/apps/{appId}/webhooks/{mode}/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testWebhook(
            @PathVariable Long appId, @PathVariable ApiMode mode, Authentication auth) {
        Map<String, Object> result = webhookService.sendTestEvent(
                appId, mode, extractUserId(auth), false);
        boolean delivered = Boolean.TRUE.equals(result.get("delivered"));
        return ResponseEntity.ok(ApiResponse.success(
                delivered ? "Test event delivered." : "Test event failed — check your URL.", result));
    }

    @Operation(
        summary     = "Get webhook delivery logs",
        description = "Returns a paginated history of all delivery attempts for the specified webhook."
    )
    @GetMapping("/apps/{appId}/webhooks/{webhookId}/logs")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getDeliveryLogs(
            @PathVariable Long appId, @PathVariable Long webhookId,
            @PageableDefault(size = 50) Pageable pageable, Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getDeliveryLogs(webhookId, pageable)));
    }

    @Operation(
        summary     = "Get all delivery logs for an app",
        description = "Returns a paginated log of every webhook delivery attempt across all webhooks for an app."
    )
    @GetMapping("/apps/{appId}/logs")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getAppLogs(
            @PathVariable Long appId,
            @PageableDefault(size = 50) Pageable pageable, Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getAppDeliveryLogs(appId, pageable)));
    }

    @Operation(
        summary     = "Request live mode access",
        description = "Submits a live-mode activation request for the app. The team will review and approve within 1–2 business days."
    )
    @PostMapping("/apps/{appId}/live/request")
    public ResponseEntity<ApiResponse<Void>> requestLiveAccess(
            @PathVariable Long appId, Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success(
                "Live access request submitted. Our team will review and approve within 1-2 business days.",
                null));
    }

    @Operation(
        summary     = "List all webhook event types",
        description = "Returns every event type the platform can deliver to a webhook (e.g. payment.success, transfer.failed)."
    )
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Object>> listEventTypes() {
        var events = java.util.Arrays.stream(
                com.epay.domain.developer.enums.WebhookEventType.values())
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
