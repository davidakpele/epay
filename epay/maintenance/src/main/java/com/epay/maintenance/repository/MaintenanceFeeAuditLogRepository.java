package com.epay.maintenance.repository;

import com.epay.domain.maintenance.entity.MaintenanceFeeAuditLog;
import com.epay.domain.maintenance.enums.MaintenanceAuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceFeeAuditLogRepository extends JpaRepository<MaintenanceFeeAuditLog, Long> {

    @Query("SELECT l FROM MaintenanceFeeAuditLog l WHERE l.batchId = :batchId ORDER BY l.createdAt ASC")
    List<MaintenanceFeeAuditLog> findByBatchId(@Param("batchId") String batchId);

    @Query("SELECT l FROM MaintenanceFeeAuditLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Page<MaintenanceFeeAuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
           SELECT l FROM MaintenanceFeeAuditLog l
           WHERE l.batchId = :batchId AND l.action = 'FAILED'
           ORDER BY l.createdAt ASC
           """)
    List<MaintenanceFeeAuditLog> findFailuresByBatchId(@Param("batchId") String batchId);

    @Query("""
           SELECT l.action, COUNT(l)
           FROM MaintenanceFeeAuditLog l
           WHERE l.batchId = :batchId
           GROUP BY l.action
           """)
    List<Object[]> countByActionForBatch(@Param("batchId") String batchId);

    @Query("SELECT l FROM MaintenanceFeeAuditLog l ORDER BY l.createdAt DESC")
    Page<MaintenanceFeeAuditLog> findAllPaged(Pageable pageable);
}
