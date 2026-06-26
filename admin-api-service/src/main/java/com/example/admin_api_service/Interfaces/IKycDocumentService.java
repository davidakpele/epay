package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.KycDocumentStatus;
import com.example.admin_api_service.enums.KycDocumentType;
import com.example.admin_api_service.models.userAndWalletManagement.KycDocument;

public interface IKycDocumentService {
    KycDocument uploadDocument(String userKycId, Long userId, KycDocumentType documentType,
                               String documentNumber, String issuingCountry,
                               String frontFileUrl, String backFileUrl, String selfieFileUrl,
                               String fileMimeType, Long fileSizeBytes);
 
    KycDocument getDocumentById(String documentId);
 
    List<KycDocument> getDocumentsForKyc(String userKycId);
 
    List<KycDocument> getDocumentsForUser(Long userId);
 
    Page<KycDocument> getDocumentsByStatus(KycDocumentStatus status, Pageable pageable);
 
    KycDocument approveDocument(String documentId, String reviewedBy, String reviewNote);
 
    KycDocument rejectDocument(String documentId, String reviewedBy, String rejectionReason);
 
    KycDocument requestResubmission(String documentId, String reviewedBy, String rejectionReason);
 
    KycDocument updateProviderResult(String documentId, String providerName,
                                     String providerReference, String providerResponse);
 
    void deleteDocument(String documentId);
}
