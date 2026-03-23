package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IUserKycService;
import com.example.admin_api_service.enums.KycStatus;
import com.example.admin_api_service.enums.KycTier;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.userAndWalletManagement.UserKyc;
import com.example.admin_api_service.repository.UserKycRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserKycServiceImpl implements IUserKycService {

    private final UserKycRepository userKycRepository;

    public UserKycServiceImpl(UserKycRepository userKycRepository) {
        this.userKycRepository = userKycRepository;
    }

    @Override
    public UserKyc initiateKyc(Long userId) {
        if (userKycRepository.existsByUserId(userId)) {
            throw new ConflictException("KYC profile already exists for userId: " + userId);
        }
        UserKyc kyc = new UserKyc();
        kyc.setUserId(userId);
        kyc.setTier(KycTier.TIER_0);
        kyc.setStatus(KycStatus.NOT_STARTED);
        return userKycRepository.save(kyc);
    }

    @Override
    @Transactional(readOnly = true)
    public UserKyc getKycById(String kycId) {
        return userKycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("UserKyc", "id", kycId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserKyc getKycByUserId(Long userId) {
        return userKycRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserKyc", "userId", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserKyc> getAllKyc(Pageable pageable) {
        return userKycRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserKyc> getKycByStatus(KycStatus status, Pageable pageable) {
        return userKycRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserKyc> getKycByTier(KycTier tier, Pageable pageable) {
        return userKycRepository.findAllByTier(tier, pageable);
    }

    @Override
    public UserKyc updateKycPersonalInfo(String kycId, UserKyc updatedInfo) {
        UserKyc kyc = getKycById(kycId);

        if (kyc.getStatus() == KycStatus.APPROVED) {
            throw new ConflictException("Cannot update personal info on an already approved KYC profile");
        }

        kyc.setFirstName(updatedInfo.getFirstName());
        kyc.setMiddleName(updatedInfo.getMiddleName());
        kyc.setLastName(updatedInfo.getLastName());
        kyc.setDateOfBirth(updatedInfo.getDateOfBirth());
        kyc.setGender(updatedInfo.getGender());
        kyc.setNationality(updatedInfo.getNationality());
        kyc.setPhoneNumber(updatedInfo.getPhoneNumber());
        kyc.setEmail(updatedInfo.getEmail());
        kyc.setAddressLine1(updatedInfo.getAddressLine1());
        kyc.setAddressLine2(updatedInfo.getAddressLine2());
        kyc.setCity(updatedInfo.getCity());
        kyc.setState(updatedInfo.getState());
        kyc.setCountry(updatedInfo.getCountry());
        kyc.setPostalCode(updatedInfo.getPostalCode());
        kyc.setNationalIdNumber(updatedInfo.getNationalIdNumber());
        kyc.setUpdatedOn(LocalDateTime.now());

        if (kyc.getStatus() == KycStatus.NOT_STARTED) {
            kyc.setStatus(KycStatus.IN_PROGRESS);
        }

        return userKycRepository.save(kyc);
    }

    @Override
    public UserKyc submitKyc(String kycId) {
        UserKyc kyc = getKycById(kycId);

        if (kyc.getStatus() == KycStatus.APPROVED) {
            throw new ConflictException("KYC is already approved");
        }
        if (kyc.getStatus() == KycStatus.UNDER_REVIEW || kyc.getStatus() == KycStatus.SUBMITTED) {
            throw new ConflictException("KYC is already submitted and under review");
        }
        if (kyc.getFirstName() == null || kyc.getLastName() == null || kyc.getDateOfBirth() == null) {
            throw new BadRequestException("Personal information is incomplete. Please fill all required fields before submitting.");
        }

        kyc.setStatus(KycStatus.SUBMITTED);
        kyc.setSubmittedAt(LocalDateTime.now());
        kyc.setUpdatedOn(LocalDateTime.now());
        return userKycRepository.save(kyc);
    }

    @Override
    public UserKyc approveKyc(String kycId, KycTier grantedTier, String reviewedBy, String reviewNote) {
        UserKyc kyc = getKycById(kycId);

        if (kyc.getStatus() != KycStatus.SUBMITTED && kyc.getStatus() != KycStatus.UNDER_REVIEW) {
            throw new ConflictException("KYC must be submitted or under review before it can be approved");
        }

        kyc.setStatus(KycStatus.APPROVED);
        kyc.setTier(grantedTier);
        kyc.setReviewedBy(reviewedBy);
        kyc.setReviewedAt(LocalDateTime.now());
        kyc.setReviewNote(reviewNote);
        kyc.setRejectionReason(null);
        kyc.setUpdatedOn(LocalDateTime.now());
        return userKycRepository.save(kyc);
    }

    @Override
    public UserKyc rejectKyc(String kycId, String reviewedBy, String rejectionReason) {
        UserKyc kyc = getKycById(kycId);

        if (kyc.getStatus() != KycStatus.SUBMITTED && kyc.getStatus() != KycStatus.UNDER_REVIEW) {
            throw new ConflictException("KYC must be submitted or under review before it can be rejected");
        }

        kyc.setStatus(KycStatus.REJECTED);
        kyc.setReviewedBy(reviewedBy);
        kyc.setReviewedAt(LocalDateTime.now());
        kyc.setRejectionReason(rejectionReason);
        kyc.setUpdatedOn(LocalDateTime.now());
        return userKycRepository.save(kyc);
    }

    @Override
    public UserKyc suspendKyc(String kycId, String reviewedBy, String reason) {
        UserKyc kyc = getKycById(kycId);
        kyc.setStatus(KycStatus.SUSPENDED);
        kyc.setReviewedBy(reviewedBy);
        kyc.setReviewedAt(LocalDateTime.now());
        kyc.setReviewNote(reason);
        kyc.setUpdatedOn(LocalDateTime.now());
        return userKycRepository.save(kyc);
    }

    @Override
    public UserKyc verifyBvn(String kycId, String bvn, String verifiedBy) {
        UserKyc kyc = getKycById(kycId);
        kyc.setBvn(bvn);
        kyc.setBvnVerified(true);
        kyc.setBvnVerifiedAt(LocalDateTime.now());
        kyc.setUpdatedOn(LocalDateTime.now());
        return userKycRepository.save(kyc);
    }

    @Override
    @Scheduled(cron = "0 0 1 * * *") // daily at 1AM
    public void expireStaleKyc() {
        List<UserKyc> expired = userKycRepository
                .findAllByStatusAndExpiresAtBefore(KycStatus.APPROVED, LocalDateTime.now());
        expired.forEach(kyc -> {
            kyc.setStatus(KycStatus.EXPIRED);
            kyc.setUpdatedOn(LocalDateTime.now());
        });
        if (!expired.isEmpty()) {
            userKycRepository.saveAll(expired);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isKycApproved(Long userId) {
        return userKycRepository.findByUserId(userId)
                .map(kyc -> kyc.getStatus() == KycStatus.APPROVED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public KycTier getCurrentTier(Long userId) {
        return userKycRepository.findByUserId(userId)
                .map(UserKyc::getTier)
                .orElse(KycTier.TIER_0);
    }

    @Override
    public boolean hasOpenKyc(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasOpenKyc'");
    }
}