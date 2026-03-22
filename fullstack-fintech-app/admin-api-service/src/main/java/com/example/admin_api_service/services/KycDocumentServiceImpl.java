package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IKycDocumentService;
import com.example.admin_api_service.Interfaces.IUserKycService;
import com.example.admin_api_service.enums.KycDocumentStatus;
import com.example.admin_api_service.enums.KycDocumentType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.userAndWalletManagement.KycDocument;
import com.example.admin_api_service.repository.KycDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class KycDocumentServiceImpl implements IKycDocumentService {

    private final KycDocumentRepository kycDocumentRepository;
    private final IUserKycService userKycService;

    public KycDocumentServiceImpl(KycDocumentRepository kycDocumentRepository,
                                   IUserKycService userKycService) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userKycService = userKycService;
    }

    @Override
    public KycDocument uploadDocument(String userKycId, Long userId, KycDocumentType documentType,
                                      String documentNumber, String issuingCountry,
                                      String frontFileUrl, String backFileUrl, String selfieFileUrl,
                                      String fileMimeType, Long fileSizeBytes) {
        userKycService.getKycById(userKycId); // validate KYC exists

        KycDocument document = new KycDocument();
        document.setUserKycId(userKycId);
        document.setUserId(userId);
        document.setDocumentType(documentType);
        document.setDocumentNumber(documentNumber);
        document.setIssuingCountry(issuingCountry);
        document.setFrontFileUrl(frontFileUrl);
        document.setBackFileUrl(backFileUrl);
        document.setSelfieFileUrl(selfieFileUrl);
        document.setFileMimeType(fileMimeType);
        document.setFileSizeBytes(fileSizeBytes);
        document.setStatus(KycDocumentStatus.PENDING);
        return kycDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public KycDocument getDocumentById(String documentId) {
        return kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("KycDocument", "id", documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocument> getDocumentsForKyc(String userKycId) {
        return kycDocumentRepository.findAllByUserKycId(userKycId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocument> getDocumentsForUser(Long userId) {
        return kycDocumentRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycDocument> getDocumentsByStatus(KycDocumentStatus status, Pageable pageable) {
        return kycDocumentRepository.findAllByStatus(status, pageable);
    }

    @Override
    public KycDocument approveDocument(String documentId, String reviewedBy, String reviewNote) {
        KycDocument document = getDocumentById(documentId);
        if (document.getStatus() == KycDocumentStatus.APPROVED) {
            throw new ConflictException("Document is already approved");
        }
        document.setStatus(KycDocumentStatus.APPROVED);
        document.setReviewedBy(reviewedBy);
        document.setReviewedAt(LocalDateTime.now());
        document.setReviewNote(reviewNote);
        document.setRejectionReason(null);
        document.setUpdatedOn(LocalDateTime.now());
        return kycDocumentRepository.save(document);
    }

    @Override
    public KycDocument rejectDocument(String documentId, String reviewedBy, String rejectionReason) {
        KycDocument document = getDocumentById(documentId);
        document.setStatus(KycDocumentStatus.REJECTED);
        document.setReviewedBy(reviewedBy);
        document.setReviewedAt(LocalDateTime.now());
        document.setRejectionReason(rejectionReason);
        document.setUpdatedOn(LocalDateTime.now());
        return kycDocumentRepository.save(document);
    }

    @Override
    public KycDocument requestResubmission(String documentId, String reviewedBy, String rejectionReason) {
        KycDocument document = getDocumentById(documentId);
        document.setStatus(KycDocumentStatus.RESUBMISSION_REQUIRED);
        document.setReviewedBy(reviewedBy);
        document.setReviewedAt(LocalDateTime.now());
        document.setRejectionReason(rejectionReason);
        document.setUpdatedOn(LocalDateTime.now());
        return kycDocumentRepository.save(document);
    }

    @Override
    public KycDocument updateProviderResult(String documentId, String providerName,
                                            String providerReference, String providerResponse) {
        KycDocument document = getDocumentById(documentId);
        document.setProviderName(providerName);
        document.setProviderReference(providerReference);
        document.setProviderResponse(providerResponse);
        document.setUpdatedOn(LocalDateTime.now());
        return kycDocumentRepository.save(document);
    }

    @Override
    public void deleteDocument(String documentId) {
        KycDocument document = getDocumentById(documentId);
        if (document.getStatus() == KycDocumentStatus.APPROVED) {
            throw new ConflictException("Approved documents cannot be deleted");
        }
        kycDocumentRepository.delete(document);
    }
}
