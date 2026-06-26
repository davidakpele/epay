package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.admin_api_service.enums.SanctionListStatus;
import com.example.admin_api_service.enums.SanctionListType;
import com.example.admin_api_service.models.complianceAndRisk.SanctionList;

@Repository
public interface SanctionListRepository extends JpaRepository<SanctionList, String> {
    Page<SanctionList> findAllByStatus(SanctionListStatus status, Pageable pageable);
    Page<SanctionList> findAllByListType(SanctionListType listType, Pageable pageable);
    Page<SanctionList> findAllByIssuingAuthority(String issuingAuthority, Pageable pageable);
    List<SanctionList> findAllByStatusAndNameContainingIgnoreCase(SanctionListStatus status, String name);
    long countByStatus(SanctionListStatus status);
 
    @Modifying
    @Query("UPDATE SanctionList s SET s.status = :status WHERE s.issuingAuthority = :authority")
    void updateStatusByIssuingAuthority(@Param("authority") String authority, @Param("status") SanctionListStatus status);
}