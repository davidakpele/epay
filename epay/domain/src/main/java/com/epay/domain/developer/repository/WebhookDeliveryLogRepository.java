package com.epay.domain.developer.repository;

import com.epay.domain.developer.entity.WebhookDeliveryLog;
import com.epay.domain.developer.enums.WebhookDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, Long> {

    Page<WebhookDeliveryLog> findByWebhookIdOrderByCreatedAtDesc(Long webhookId, Pageable pageable);

    Page<WebhookDeliveryLog> findByAppIdOrderByCreatedAtDesc(Long appId, Pageable pageable);

    @Query("SELECT l FROM WebhookDeliveryLog l WHERE l.status = 'RETRYING' AND l.nextRetryAt <= :now")
    List<WebhookDeliveryLog> findDueRetries(@Param("now") LocalDateTime now);

    boolean existsByEventIdAndStatus(String eventId, WebhookDeliveryStatus status);
}
