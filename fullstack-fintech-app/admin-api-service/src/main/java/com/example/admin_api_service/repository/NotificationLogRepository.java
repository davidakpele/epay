package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.models.notificationsAndComms.NotificationLog;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

    Page<NotificationLog> findAllByUserId(Long userId, Pageable pageable);

    Page<NotificationLog> findAllByStatus(NotificationLogStatus status, Pageable pageable);

    Page<NotificationLog> findAllByChannel(NotificationChannel channel, Pageable pageable);

    // sourceEntityId is the model field — was incorrectly referenceId in the original
    Page<NotificationLog> findAllBySourceEntityId(String sourceEntityId, Pageable pageable);

    // Picks up all PENDING logs that either have no schedule or whose schedule time has arrived
    @Query("SELECT n FROM NotificationLog n " +
           "WHERE n.status = :status " +
           "AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)")
    List<NotificationLog> findAllByStatusAndScheduledAtBeforeOrEqual(
            @Param("status") NotificationLogStatus status,
            @Param("now") LocalDateTime now);
}