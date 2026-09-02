package com.epay.domain.developer.repository;

import com.epay.domain.developer.entity.AppWebhook;
import com.epay.domain.developer.enums.ApiMode;
import com.epay.domain.developer.enums.WebhookEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppWebhookRepository extends JpaRepository<AppWebhook, Long> {

    List<AppWebhook> findByAppIdAndModeAndActiveTrue(Long appId, ApiMode mode);

    Optional<AppWebhook> findByAppIdAndMode(Long appId, ApiMode mode);

    @Query("""
           SELECT w FROM AppWebhook w
           WHERE w.active = true
             AND (w.subscribedEvents IS NULL
               OR w.subscribedEvents LIKE CONCAT('%', :eventType, '%'))
           """)
    List<AppWebhook> findActiveSubscribersForEvent(@Param("eventType") String eventType);

    @Modifying
    @Query("UPDATE AppWebhook w SET w.successCount = w.successCount + 1, w.lastDeliveryAt = :now, w.lastDeliveryStatus = 'DELIVERED' WHERE w.id = :id")
    void incrementSuccess(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AppWebhook w SET w.failureCount = w.failureCount + 1, w.lastDeliveryAt = :now, w.lastDeliveryStatus = 'FAILED' WHERE w.id = :id")
    void incrementFailure(@Param("id") Long id, @Param("now") LocalDateTime now);
}
