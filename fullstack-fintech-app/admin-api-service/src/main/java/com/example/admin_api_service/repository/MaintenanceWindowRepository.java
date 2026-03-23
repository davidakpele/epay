package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.MaintenanceWindowStatus;
import com.example.admin_api_service.models.systemAndConfiguration.MaintenanceWindow;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, String> {
    Page<MaintenanceWindow> findAllByStatus(MaintenanceWindowStatus status, Pageable pageable);
    List<MaintenanceWindow> findAllByStatusAndScheduledStartAfterOrderByScheduledStartAsc(
            MaintenanceWindowStatus status, LocalDateTime after);
    Optional<MaintenanceWindow> findFirstByStatus(MaintenanceWindowStatus status);
    boolean existsByStatus(MaintenanceWindowStatus status);
 
    @Query("SELECT w FROM MaintenanceWindow w WHERE w.status = :status AND w.scheduledStart <= :now")
    List<MaintenanceWindow> findAllByStatusAndScheduledStartBeforeOrEqual(
            @Param("status") MaintenanceWindowStatus status, @Param("now") LocalDateTime now);
 
    List<MaintenanceWindow> findAllByStatusAndScheduledEndBefore(
            MaintenanceWindowStatus status, LocalDateTime now);
}