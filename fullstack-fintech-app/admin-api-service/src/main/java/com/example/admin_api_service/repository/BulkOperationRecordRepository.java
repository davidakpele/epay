package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.BulkRecordStatus;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationRecord;

@Repository
public interface BulkOperationRecordRepository extends JpaRepository<BulkOperationRecord, String> {
    Page<BulkOperationRecord> findAllByJobId(String jobId, Pageable pageable);
    Page<BulkOperationRecord> findAllByJobIdAndStatus(String jobId, BulkRecordStatus status, Pageable pageable);
    List<BulkOperationRecord> findAllByJobIdAndStatus(String jobId, BulkRecordStatus status);
}
