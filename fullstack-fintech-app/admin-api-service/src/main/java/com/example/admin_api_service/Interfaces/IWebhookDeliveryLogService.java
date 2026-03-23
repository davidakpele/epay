package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.WebhookDeliveryStatus;
import com.example.admin_api_service.models.notificationsAndComms.WebhookDeliveryLog;

public interface IWebhookDeliveryLogService {
    // Create a delivery attempt log and trigger dispatch
    WebhookDeliveryLog deliver(String webhookEndpointId, String eventType,
                               String eventReferenceId, String eventReferenceType,
                               String requestPayload);
 
    WebhookDeliveryLog getLogById(String logId);
 
    Page<WebhookDeliveryLog> getAllLogs(Pageable pageable);
 
    Page<WebhookDeliveryLog> getLogsByEndpoint(String endpointId, Pageable pageable);
 
    Page<WebhookDeliveryLog> getLogsByStatus(WebhookDeliveryStatus status, Pageable pageable);
 
    Page<WebhookDeliveryLog> getLogsByEventType(String eventType, Pageable pageable);
 
    List<WebhookDeliveryLog> getLogsForEventReference(String eventReferenceId);
 
    // Called when the HTTP response is received from the endpoint
    WebhookDeliveryLog recordResponse(String logId, int httpStatusCode,
                                      String responseBody, String responseHeaders,
                                      long durationMs);
 
    // Called when delivery attempt times out or throws a connection error
    WebhookDeliveryLog recordError(String logId, String failureReason);
 
    // Manually replay a failed delivery (admin-triggered)
    WebhookDeliveryLog replay(String logId, String replayedBy);
 
    // Process pending retries whose nextRetryAt has elapsed
    void processRetries();
}
