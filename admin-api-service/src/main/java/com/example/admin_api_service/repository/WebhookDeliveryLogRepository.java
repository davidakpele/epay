package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.WebhookDeliveryStatus;
import com.example.admin_api_service.models.notificationsAndComms.WebhookDeliveryLog;

@Repository
public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, String> {
    Page<WebhookDeliveryLog> findAllByWebhookEndpointId(String endpointId, Pageable pageable);
    Page<WebhookDeliveryLog> findAllByStatus(WebhookDeliveryStatus status, Pageable pageable);
    Page<WebhookDeliveryLog> findAllByEventType(String eventType, Pageable pageable);
    List<WebhookDeliveryLog> findAllByEventReferenceId(String eventReferenceId);
    Optional<WebhookDeliveryLog> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKeyAndStatusIn(String idempotencyKey, List<WebhookDeliveryStatus> statuses);
 
    @Query("SELECT d FROM WebhookDeliveryLog d " +
           "WHERE d.status = :status " +
           "AND d.nextRetryAt <= :now")
    List<WebhookDeliveryLog> findAllByStatusAndNextRetryAtBeforeOrEqual(
            @Param("status") WebhookDeliveryStatus status,
            @Param("now") LocalDateTime now);
}