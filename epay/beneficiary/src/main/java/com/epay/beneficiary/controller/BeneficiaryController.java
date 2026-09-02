package com.epay.beneficiary.controller;

import com.epay.beneficiary.service.BeneficiaryService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.beneficiary.dto.BeneficiaryDTO;
import com.epay.domain.beneficiary.enums.BeneficiaryType;
import com.epay.domain.beneficiary.input.CreateBeneficiaryRequest;
import com.epay.domain.beneficiary.input.DeleteBeneficiariesRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for managing saved beneficiaries.
 *
 * <p>Base path: {@code /beneficiaries}
 *
 * <table border="1">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td>  <td>/beneficiaries/create</td>                           <td>Save a new beneficiary</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{userId}/all</td>                     <td>List all beneficiaries for a user</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{id}</td>                             <td>Get a beneficiary by its record id</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{userId}/type?type=BANK|USER</td>     <td>Filter by type</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{userId}/search?q=term</td>           <td>Search by name / account / username</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{id}/verify?userId=</td>              <td>Verify a beneficiary record exists</td></tr>
 *   <tr><td>GET</td>   <td>/beneficiaries/{userId}/username/{username}</td>     <td>Look up a user-type beneficiary by username</td></tr>
 *   <tr><td>PUT</td>   <td>/beneficiaries/{id}</td>                             <td>Update a beneficiary</td></tr>
 *   <tr><td>DELETE</td><td>/beneficiaries/{id}?userId=</td>                     <td>Soft-delete a single beneficiary</td></tr>
 *   <tr><td>DELETE</td><td>/beneficiaries/bulk</td>                             <td>Soft-delete multiple beneficiaries</td></tr>
 * </table>
 */
@Slf4j
@RestController
@RequestMapping("/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * POST /beneficiaries/create
     * Save a new beneficiary for a user.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> create(
            @Valid @RequestBody CreateBeneficiaryRequest req) {

        validateCreateRequest(req);
        BeneficiaryDTO dto = beneficiaryService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created successfully", dto));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * GET /beneficiaries/{userId}/all
     * List all active beneficiaries for a user.
     */
    @GetMapping("/{userId}/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BeneficiaryDTO>>> getAllByUserId(
            @PathVariable Long userId) {

        List<BeneficiaryDTO> list = beneficiaryService.getAllByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries retrieved successfully", list));
    }

    /**
     * GET /beneficiaries/{id}?userId=
     * Get a single beneficiary by its record id, scoped to the userId.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> getById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        BeneficiaryDTO dto = beneficiaryService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary retrieved successfully", dto));
    }

    /**
     * GET /beneficiaries/{userId}/type?type=BANK|USER
     * Return beneficiaries filtered by type.
     */
    @GetMapping("/{userId}/type")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BeneficiaryDTO>>> getByType(
            @PathVariable Long userId,
            @RequestParam String type) {

        BeneficiaryType beneficiaryType;
        try {
            beneficiaryType = BeneficiaryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Type must be 'BANK' or 'USER'", ErrorCode.INVALID_INPUT);
        }

        List<BeneficiaryDTO> list = beneficiaryService.getByType(userId, beneficiaryType);
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries retrieved successfully", list));
    }

    /**
     * GET /beneficiaries/{userId}/search?q=term
     * Search across beneficiary name, account number, and recipient username.
     */
    @GetMapping("/{userId}/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BeneficiaryDTO>>> search(
            @PathVariable Long userId,
            @RequestParam("q") String q) {

        if (q == null || q.isBlank())
            throw new BadRequestException("Provide a search query parameter", ErrorCode.INVALID_INPUT);

        List<BeneficiaryDTO> list = beneficiaryService.search(userId, q);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", list));
    }

    /**
     * GET /beneficiaries/{id}/verify?userId=
     * Verify that a beneficiary exists and belongs to the given user.
     */
    @GetMapping("/{id}/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> verify(
            @PathVariable Long id,
            @RequestParam Long userId) {

        BeneficiaryDTO dto = beneficiaryService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary verified", dto));
    }

    /**
     * GET /beneficiaries/{userId}/username/{recipientUsername}
     * Look up a user-type beneficiary by the recipient's ePay username.
     */
    @GetMapping("/{userId}/username/{recipientUsername}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> getByUsername(
            @PathVariable Long userId,
            @PathVariable String recipientUsername) {

        if (recipientUsername == null || recipientUsername.isBlank())
            throw new BadRequestException("recipientUsername is required", ErrorCode.INVALID_INPUT);

        BeneficiaryDTO dto = beneficiaryService.getByUsername(userId, recipientUsername);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary retrieved successfully", dto));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * PUT /beneficiaries/{id}
     * Update an existing beneficiary.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateBeneficiaryRequest req) {

        validateCreateRequest(req);
        BeneficiaryDTO dto = beneficiaryService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", dto));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * DELETE /beneficiaries/{id}?userId=
     * Soft-delete a single beneficiary.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long userId) {

        beneficiaryService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }

    /**
     * DELETE /beneficiaries/bulk
     * Soft-delete multiple beneficiaries in one call.
     */
    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteByIds(
            @Valid @RequestBody DeleteBeneficiariesRequest req) {

        beneficiaryService.deleteByIds(req.getIds(), req.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries deleted successfully", null));
    }

    // ── Validation helper ─────────────────────────────────────────────────────

    /**
     * Type-specific field validation that cannot be expressed with annotations alone,
     * matching the original .NET controller's explicit checks.
     */
    private void validateCreateRequest(CreateBeneficiaryRequest req) {
        if (req.getBeneficiaryType() == BeneficiaryType.BANK) {
            if (req.getAccountNumber() == null || req.getAccountNumber().isBlank())
                throw new BadRequestException(
                        "accountNumber is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getAccountName() == null || req.getAccountName().isBlank())
                throw new BadRequestException(
                        "accountName is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getBankCode() == null || req.getBankCode().isBlank())
                throw new BadRequestException(
                        "bankCode is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getBankName() == null || req.getBankName().isBlank())
                throw new BadRequestException(
                        "bankName is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
        } else if (req.getBeneficiaryType() == BeneficiaryType.USER) {
            if (req.getRecipientUsername() == null || req.getRecipientUsername().isBlank())
                throw new BadRequestException(
                        "recipientUsername is required for user beneficiaries", ErrorCode.INVALID_INPUT);
        }
    }
}
