package com.example.admin_api_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.ServiceHealthStatus;
import com.example.admin_api_service.models.systemAndConfiguration.ServiceHealthLog;

@Repository
public interface ServiceHealthLogRepository extends JpaRepository<ServiceHealthLog, String> {
    Page<ServiceHealthLog> findAllByServiceNameOrderByCheckedAtDesc(String serviceName, Pageable pageable);
    Page<ServiceHealthLog> findAllByStatus(ServiceHealthStatus status, Pageable pageable);
    Optional<ServiceHealthLog> findTopByServiceNameOrderByCheckedAtDesc(String serviceName);
    List<ServiceHealthLog> findAllByAlertTriggedTrueOrderByCheckedAtDesc(Pageable pageable);
 
    // Latest log per distinct service name
    @Query("SELECT s FROM ServiceHealthLog s WHERE s.checkedAt = " +
           "(SELECT MAX(s2.checkedAt) FROM ServiceHealthLog s2 WHERE s2.serviceName = s.serviceName)")
    List<ServiceHealthLog> findLatestPerService();
 
    @Modifying
    @Query("DELETE FROM ServiceHealthLog s WHERE s.checkedAt < :cutoff")
    void deleteAllByCheckedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}