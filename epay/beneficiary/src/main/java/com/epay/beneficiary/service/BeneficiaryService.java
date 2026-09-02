package com.epay.beneficiary.service;

import com.epay.beneficiary.repository.BeneficiaryRepository;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.beneficiary.dto.BeneficiaryDTO;
import com.epay.domain.beneficiary.entity.Beneficiary;
import com.epay.domain.beneficiary.enums.BeneficiaryType;
import com.epay.domain.beneficiary.input.CreateBeneficiaryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserLookupPort        userLookupPort;
    private final UserRepository        userRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public BeneficiaryDTO create(CreateBeneficiaryRequest req) {
        validateOwner(req.getUserId());

        String currency = (req.getCurrency() != null && !req.getCurrency().isBlank())
                ? req.getCurrency().toUpperCase() : "NGN";

        Beneficiary beneficiary = Beneficiary.builder()
                .userId(req.getUserId())
                .beneficiaryType(req.getBeneficiaryType())
                .beneficiaryName(req.getBeneficiaryName().trim())
                .currency(currency)
                .isActive(true)
                .build();

        if (req.getBeneficiaryType() == BeneficiaryType.BANK) {
            // Duplicate check — same account number for this user
            beneficiaryRepository
                    .findByUserIdAndAccountNumberAndBeneficiaryTypeAndIsActiveTrue(
                            req.getUserId(), req.getAccountNumber(), BeneficiaryType.BANK)
                    .ifPresent(b -> { throw new BadRequestException(
                            "A bank beneficiary with this account number already exists",
                            ErrorCode.BENEFICIARY_ALREADY_EXISTS); });

            beneficiary.setAccountNumber(req.getAccountNumber().trim());
            beneficiary.setAccountName(req.getAccountName().trim());
            beneficiary.setBankCode(req.getBankCode().trim());
            beneficiary.setBankName(req.getBankName().trim());
            beneficiary.setRecipientUsername(null);

        } else {
            // Verify the target ePay user actually exists
            if (!userRepository.existsByUsername(req.getRecipientUsername())) {
                throw new BadRequestException(
                        "Recipient username '" + req.getRecipientUsername() + "' does not exist",
                        ErrorCode.USER_NOT_FOUND);
            }

            // Duplicate check — same username for this user
            beneficiaryRepository
                    .findByUserIdAndRecipientUsernameAndBeneficiaryTypeAndIsActiveTrue(
                            req.getUserId(), req.getRecipientUsername(), BeneficiaryType.USER)
                    .ifPresent(b -> { throw new BadRequestException(
                            "A user beneficiary with this username already exists",
                            ErrorCode.BENEFICIARY_ALREADY_EXISTS); });

            beneficiary.setRecipientUsername(req.getRecipientUsername().trim());
            beneficiary.setAccountNumber(null);
            beneficiary.setAccountName(null);
            beneficiary.setBankCode(null);
            beneficiary.setBankName(null);
        }

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("[Beneficiary] Created id={} type={} userId={}", saved.getId(),
                saved.getBeneficiaryType(), saved.getUserId());
        return toDTO(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Returns the first active beneficiary record owned by this userId. */
    @Transactional(readOnly = true)
    public BeneficiaryDTO getByUserId(Long userId) {
        return beneficiaryRepository.findFirstByUserIdAndIsActiveTrue(userId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
    }

    /** Returns a single beneficiary by its own id, scoped to the userId. */
    @Transactional(readOnly = true)
    public BeneficiaryDTO getById(Long id, Long userId) {
        return beneficiaryRepository.findByIdAndUserIdAndIsActiveTrue(id, userId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
    }

    /** Returns all active beneficiaries for a user. */
    @Transactional(readOnly = true)
    public List<BeneficiaryDTO> getAllByUserId(Long userId) {
        return beneficiaryRepository
                .findByUserIdAndIsActiveTrueOrderByBeneficiaryNameAsc(userId)
                .stream().map(this::toDTO).toList();
    }

    /** Returns active beneficiaries filtered by type. */
    @Transactional(readOnly = true)
    public List<BeneficiaryDTO> getByType(Long userId, BeneficiaryType type) {
        return beneficiaryRepository
                .findByUserIdAndBeneficiaryTypeAndIsActiveTrueOrderByBeneficiaryNameAsc(userId, type)
                .stream().map(this::toDTO).toList();
    }

    /** Full-text search across name, account number, and recipient username. */
    @Transactional(readOnly = true)
    public List<BeneficiaryDTO> search(Long userId, String term) {
        return beneficiaryRepository.search(userId, term.trim())
                .stream().map(this::toDTO).toList();
    }

    /** Look up a user-type beneficiary by the recipient's username. */
    @Transactional(readOnly = true)
    public BeneficiaryDTO getByUsername(Long userId, String recipientUsername) {
        return beneficiaryRepository
                .findByUserIdAndRecipientUsernameAndIsActiveTrue(userId, recipientUsername)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public BeneficiaryDTO update(Long id, CreateBeneficiaryRequest req) {
        Beneficiary existing = beneficiaryRepository
                .findByIdAndUserIdAndIsActiveTrue(id, req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        String currency = (req.getCurrency() != null && !req.getCurrency().isBlank())
                ? req.getCurrency().toUpperCase() : existing.getCurrency();

        existing.setBeneficiaryType(req.getBeneficiaryType());
        existing.setBeneficiaryName(req.getBeneficiaryName().trim());
        existing.setCurrency(currency);

        if (req.getBeneficiaryType() == BeneficiaryType.BANK) {
            existing.setAccountNumber(req.getAccountNumber().trim());
            existing.setAccountName(req.getAccountName().trim());
            existing.setBankCode(req.getBankCode().trim());
            existing.setBankName(req.getBankName().trim());
            existing.setRecipientUsername(null);
        } else {
            if (!userRepository.existsByUsername(req.getRecipientUsername())) {
                throw new BadRequestException(
                        "Recipient username '" + req.getRecipientUsername() + "' does not exist",
                        ErrorCode.USER_NOT_FOUND);
            }
            existing.setRecipientUsername(req.getRecipientUsername().trim());
            existing.setAccountNumber(null);
            existing.setAccountName(null);
            existing.setBankCode(null);
            existing.setBankName(null);
        }

        Beneficiary saved = beneficiaryRepository.save(existing);
        log.info("[Beneficiary] Updated id={} userId={}", saved.getId(), saved.getUserId());
        return toDTO(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** Soft-deletes a single beneficiary. */
    @Transactional
    public void delete(Long id, Long userId) {
        Beneficiary b = beneficiaryRepository
                .findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        b.setActive(false);
        beneficiaryRepository.save(b);
        log.info("[Beneficiary] Deleted id={} userId={}", id, userId);
    }

    /** Bulk soft-delete by a list of ids. */
    @Transactional
    public void deleteByIds(List<Long> ids, Long userId) {
        beneficiaryRepository.softDeleteByIdsAndUserId(ids, userId);
        log.info("[Beneficiary] Bulk deleted ids={} userId={}", ids, userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateOwner(Long userId) {
        if (!userLookupPort.existsActiveUser(userId))
            throw new ResourceNotFoundException("User not found or account inactive");
    }

    private BeneficiaryDTO toDTO(Beneficiary b) {
        return BeneficiaryDTO.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .beneficiaryType(b.getBeneficiaryType())
                .beneficiaryName(b.getBeneficiaryName())
                .currency(b.getCurrency())
                .accountNumber(b.getAccountNumber())
                .accountName(b.getAccountName())
                .bankCode(b.getBankCode())
                .bankName(b.getBankName())
                .recipientUsername(b.getRecipientUsername())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
