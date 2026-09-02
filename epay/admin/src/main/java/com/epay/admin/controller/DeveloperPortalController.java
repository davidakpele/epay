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


@RestController
@RequestMapping("/developer")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class DeveloperPortalController {

    private final DeveloperKeyService  keyService;
    private final AppWebhookService    webhookService;

    @PostMapping("/apps")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> createApp(
            @Valid @RequestBody CreateAppRequest request,
            Authentication auth) {
        Long userId = extractUserId(auth);
        DeveloperAppDTO app = keyService.createApp(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "App created. Copy your secret keys now — they will not be shown again.", app));
    }

    @GetMapping("/apps")
    public ResponseEntity<ApiResponse<List<DeveloperAppDTO>>> listMyApps(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                keyService.getMyApps(extractUserId(auth))));
    }

    @GetMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> getApp(
            @PathVariable Long appId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                keyService.getApp(appId, extractUserId(auth), false)));
    }

    @PutMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<DeveloperAppDTO>> updateApp(
            @PathVariable Long appId,
            @Valid @RequestBody CreateAppRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("App updated.",
                keyService.updateApp(appId, request, extractUserId(auth))));
    }

    @DeleteMapping("/apps/{appId}")
    public ResponseEntity<ApiResponse<Void>> deleteApp(
            @PathVariable Long appId, Authentication auth) {
        keyService.deleteApp(appId, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("App deleted.", null));
    }


    @PostMapping("/apps/{appId}/keys/{mode}/rotate")
    public ResponseEntity<ApiResponse<ApiKeyDTO>> rotateKey(
            @PathVariable Long appId,
            @PathVariable ApiMode mode,
            Authentication auth) {
        ApiKeyDTO newKey = keyService.rotateKey(appId, mode, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success(
                "Key rotated. Copy your new secret key — it will not be shown again.", newKey));
    }

    @DeleteMapping("/apps/{appId}/keys/{keyId}")
    public ResponseEntity<ApiResponse<Void>> revokeKey(
            @PathVariable Long appId,
            @PathVariable Long keyId,
            @RequestParam(defaultValue = "Revoked by developer") String reason,
            Authentication auth) {
        keyService.revokeKey(keyId, extractUserId(auth), false, reason);
        return ResponseEntity.ok(ApiResponse.success("API key revoked.", null));
    }


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


    @GetMapping("/apps/{appId}/webhooks/{mode}")
    public ResponseEntity<ApiResponse<WebhookDTO>> getWebhook(
            @PathVariable Long appId,
            @PathVariable ApiMode mode,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getWebhook(appId, mode, extractUserId(auth), false)));
    }

    @DeleteMapping("/apps/{appId}/webhooks/{mode}")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @PathVariable Long appId,
            @PathVariable ApiMode mode,
            Authentication auth) {
        webhookService.deleteWebhook(appId, mode, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success("Webhook deactivated.", null));
    }


    @PostMapping("/apps/{appId}/webhooks/{mode}/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testWebhook(
            @PathVariable Long appId,
            @PathVariable ApiMode mode,
            Authentication auth) {
        Map<String, Object> result = webhookService.sendTestEvent(
                appId, mode, extractUserId(auth), false);
        boolean delivered = Boolean.TRUE.equals(result.get("delivered"));
        return ResponseEntity.ok(ApiResponse.success(
                delivered ? "Test event delivered." : "Test event failed — check your URL.",
                result));
    }

    @GetMapping("/apps/{appId}/webhooks/{webhookId}/logs")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getDeliveryLogs(
            @PathVariable Long appId,
            @PathVariable Long webhookId,
            @PageableDefault(size = 50) Pageable pageable,
            Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getDeliveryLogs(webhookId, pageable)));
    }

    @GetMapping("/apps/{appId}/logs")
    public ResponseEntity<ApiResponse<Page<WebhookDeliveryLog>>> getAppLogs(
            @PathVariable Long appId,
            @PageableDefault(size = 50) Pageable pageable,
            Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false); // ownership guard
        return ResponseEntity.ok(ApiResponse.success(null,
                webhookService.getAppDeliveryLogs(appId, pageable)));
    }


    @PostMapping("/apps/{appId}/live/request")
    public ResponseEntity<ApiResponse<Void>> requestLiveAccess(@PathVariable Long appId,
                                                                 Authentication auth) {
        keyService.getApp(appId, extractUserId(auth), false);
        return ResponseEntity.ok(ApiResponse.success(
                "Live access request submitted. Our team will review and approve within 1-2 business days.",
                null));
    }

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
