package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; 
import java.util.List;
import com.example.admin_api_service.enums.WebhookEndpointStatus;
import com.example.admin_api_service.models.notificationsAndComms.WebhookEndpoint;

public interface IWebhookEndpointService {
    WebhookEndpoint registerEndpoint(WebhookEndpoint endpoint, String createdBy);
 
    WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpoint updated, String updatedBy);
 
    WebhookEndpoint getEndpointById(String endpointId);
 
    Page<WebhookEndpoint> getAllEndpoints(Pageable pageable);
 
    Page<WebhookEndpoint> getEndpointsByStatus(WebhookEndpointStatus status, Pageable pageable);
 
    List<WebhookEndpoint> getEndpointsByOwner(String ownerId, String ownerType);
 
    // All active endpoints subscribed to a specific event type
    List<WebhookEndpoint> getSubscribersForEvent(String eventType);
 
    void activateEndpoint(String endpointId, String updatedBy);
 
    void suspendEndpoint(String endpointId, String updatedBy);
 
    // Called after a successful delivery — resets consecutive failure counter
    void recordSuccess(String endpointId);
 
    // Called after a failed delivery — increments counter and auto-disables if threshold reached
    void recordFailure(String endpointId);
 
    // Rotate the HMAC secret — returns the new plain-text secret (shown once only)
    String rotateSecret(String endpointId, String updatedBy);
 
    void deleteEndpoint(String endpointId);
 
    // Validate an incoming webhook signature against the stored secret hash
    boolean validateSignature(String endpointId, String payload, String incomingSignature);
}
