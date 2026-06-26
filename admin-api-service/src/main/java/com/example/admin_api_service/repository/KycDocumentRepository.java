package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.KycDocumentStatus;
import com.example.admin_api_service.models.userAndWalletManagement.KycDocument;
import java.util.List;


@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, String> {
    List<KycDocument> findAllByUserKycId(String userKycId);
    List<KycDocument> findAllByUserId(Long userId);
    Page<KycDocument> findAllByStatus(KycDocumentStatus status, Pageable pageable);
}