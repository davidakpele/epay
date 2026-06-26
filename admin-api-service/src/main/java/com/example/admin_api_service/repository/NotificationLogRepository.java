package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.models.notificationsAndComms.NotificationLog;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

    Page<NotificationLog> findAllByUserId(Long userId, Pageable pageable);

    Page<NotificationLog> findAllByStatus(NotificationLogStatus status, Pageable pageable);

    Page<NotificationLog> findAllByChannel(NotificationChannel channel, Pageable pageable);

    Page<NotificationLog> findAllByReferenceId(String referenceId, Pageable pageable);

    @Query("SELECT n FROM NotificationLog n " +
           "WHERE n.status = :status " +
           "AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)")
    List<NotificationLog> findAllByStatusAndScheduledAtBeforeOrEqual(
            @Param("status") NotificationLogStatus status,
            @Param("now") LocalDateTime now);
}