package com.example.admin_api_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.WebhookEndpointStatus;
import com.example.admin_api_service.models.notificationsAndComms.WebhookEndpoint;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, String> {
    Page<WebhookEndpoint> findAllByStatus(WebhookEndpointStatus status, Pageable pageable);
    
    List<WebhookEndpoint> findAllByOwnerIdAndOwnerType(String ownerId, String ownerType);
 
    @Query("SELECT e FROM WebhookEndpoint e " +
           "WHERE e.status = 'ACTIVE' " +
           "AND JSON_CONTAINS(e.subscribedEvents, :eventType)")
    List<WebhookEndpoint> findActiveSubscribersForEvent(
            @Param("eventType") String eventType);
}