package com.epay.beneficiary.controller;

import com.epay.beneficiary.service.BeneficiaryService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.beneficiary.dto.BeneficiaryDTO;
import com.epay.domain.beneficiary.enums.BeneficiaryType;
import com.epay.domain.beneficiary.input.CreateBeneficiaryRequest;
import com.epay.domain.beneficiary.input.DeleteBeneficiariesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Beneficiaries", description = "Save and manage transfer beneficiaries (internal users and external banks)")
@Slf4j
@RestController
@RequestMapping("/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @Operation(
        summary     = "Save a new beneficiary",
        description = "Creates a beneficiary record. For BANK type, accountNumber, accountName, bankCode and bankName are required. For USER type, recipientUsername is required."
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> create(
            @Valid @RequestBody CreateBeneficiaryRequest req) {
        validateCreateRequest(req);
        BeneficiaryDTO dto = beneficiaryService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created successfully", dto));
    }

    @Operation(
        summary     = "List all beneficiaries for a user",
        description = "Returns every saved beneficiary (BANK and USER types) for the given user ID."
    )
    @GetMapping("/{userId}/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BeneficiaryDTO>>> getAllByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries retrieved successfully",
                beneficiaryService.getAllByUserId(userId)));
    }

    @Operation(
        summary     = "Get a beneficiary by ID",
        description = "Returns a single beneficiary record for the given ID, scoped to the requesting user."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> getById(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Beneficiary retrieved successfully",
                beneficiaryService.getById(id, userId)));
    }

    @Operation(
        summary     = "List beneficiaries by type",
        description = "Filters the user's beneficiaries by type. Valid values: BANK or USER."
    )
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
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries retrieved successfully",
                beneficiaryService.getByType(userId, beneficiaryType)));
    }

    @Operation(
        summary     = "Search beneficiaries",
        description = "Full-text search across a user's beneficiaries by name, account number, or username."
    )
    @GetMapping("/{userId}/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<BeneficiaryDTO>>> search(
            @PathVariable Long userId,
            @RequestParam("q") String q) {
        if (q == null || q.isBlank())
            throw new BadRequestException("Provide a search query parameter", ErrorCode.INVALID_INPUT);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully",
                beneficiaryService.search(userId, q)));
    }

    @Operation(
        summary     = "Verify a beneficiary",
        description = "Confirms that a saved beneficiary record is still valid and active."
    )
    @GetMapping("/{id}/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> verify(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Beneficiary verified",
                beneficiaryService.getById(id, userId)));
    }

    @Operation(
        summary     = "Get beneficiary by recipient username",
        description = "Looks up a saved USER-type beneficiary by the recipient's username."
    )
    @GetMapping("/{userId}/username/{recipientUsername}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> getByUsername(
            @PathVariable Long userId,
            @PathVariable String recipientUsername) {
        if (recipientUsername == null || recipientUsername.isBlank())
            throw new BadRequestException("recipientUsername is required", ErrorCode.INVALID_INPUT);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary retrieved successfully",
                beneficiaryService.getByUsername(userId, recipientUsername)));
    }

    @Operation(
        summary     = "Update a beneficiary",
        description = "Replaces an existing beneficiary's details with the supplied data."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BeneficiaryDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateBeneficiaryRequest req) {
        validateCreateRequest(req);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully",
                beneficiaryService.update(id, req)));
    }

    @Operation(
        summary     = "Delete a beneficiary",
        description = "Removes a single beneficiary record for the given user."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long userId) {
        beneficiaryService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }

    @Operation(
        summary     = "Delete multiple beneficiaries",
        description = "Removes a list of beneficiary IDs for the given user in a single request."
    )
    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteByIds(
            @Valid @RequestBody DeleteBeneficiariesRequest req) {
        beneficiaryService.deleteByIds(req.getIds(), req.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Beneficiaries deleted successfully", null));
    }

    private void validateCreateRequest(CreateBeneficiaryRequest req) {
        if (req.getBeneficiaryType() == BeneficiaryType.BANK) {
            if (req.getAccountNumber() == null || req.getAccountNumber().isBlank())
                throw new BadRequestException("accountNumber is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getAccountName() == null || req.getAccountName().isBlank())
                throw new BadRequestException("accountName is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getBankCode() == null || req.getBankCode().isBlank())
                throw new BadRequestException("bankCode is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
            if (req.getBankName() == null || req.getBankName().isBlank())
                throw new BadRequestException("bankName is required for bank beneficiaries", ErrorCode.INVALID_INPUT);
        } else if (req.getBeneficiaryType() == BeneficiaryType.USER) {
            if (req.getRecipientUsername() == null || req.getRecipientUsername().isBlank())
                throw new BadRequestException("recipientUsername is required for user beneficiaries", ErrorCode.INVALID_INPUT);
        }
    }
}
