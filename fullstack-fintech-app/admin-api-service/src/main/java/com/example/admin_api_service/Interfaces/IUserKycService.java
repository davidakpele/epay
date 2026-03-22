package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.KycStatus;
import com.example.admin_api_service.enums.KycTier;
import com.example.admin_api_service.models.userAndWalletManagement.UserKyc;

public interface IUserKycService {
    UserKyc initiateKyc(Long userId);
 
    UserKyc getKycById(String kycId);
 
    UserKyc getKycByUserId(Long userId);
 
    Page<UserKyc> getAllKyc(Pageable pageable);
 
    Page<UserKyc> getKycByStatus(KycStatus status, Pageable pageable);
 
    Page<UserKyc> getKycByTier(KycTier tier, Pageable pageable);
 
    UserKyc updateKycPersonalInfo(String kycId, UserKyc updatedInfo);
 
    UserKyc submitKyc(String kycId);
 
    UserKyc approveKyc(String kycId, KycTier grantedTier, String reviewedBy, String reviewNote);
 
    UserKyc rejectKyc(String kycId, String reviewedBy, String rejectionReason);
 
    UserKyc suspendKyc(String kycId, String reviewedBy, String reason);
 
    UserKyc verifyBvn(String kycId, String bvn, String verifiedBy);
 
    void expireStaleKyc();
 
    boolean isKycApproved(Long userId);
 
    KycTier getCurrentTier(Long userId);
}
