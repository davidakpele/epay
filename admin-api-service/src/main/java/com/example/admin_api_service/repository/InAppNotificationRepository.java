package com.example.admin_api_service.repository;


import com.example.admin_api_service.models.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    List<InAppNotification> findByAdminIdAndIsReadFalse(Long adminId);

    Page<InAppNotification> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);

    @Modifying
    @Query("UPDATE InAppNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.adminId = :adminId AND n.isRead = false")
    void markAllAsRead(@Param("adminId") Long adminId);

    @Modifying
    @Query("UPDATE InAppNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    long countByAdminIdAndIsReadFalse(Long adminId);
}