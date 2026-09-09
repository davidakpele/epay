package com.epay.virtual_card.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.epay.domain.virtual_card.input.CreateVirtualCardRequest;
import com.epay.domain.virtual_card.input.UpdateBalanceRequest;
import com.epay.domain.virtual_card.input.UpdateCardStatusRequest;
import com.epay.domain.virtual_card.input.UpdateVirtualCardRequest;
import com.epay.virtual_card.responses.VirtualCardDetailsResponse;
import com.epay.virtual_card.responses.VirtualCardResponse;
import com.epay.virtual_card.service.VirtualCardService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/virtual-cards")
@Tag(name = "Virtual Card Management", description = "APIs for managing virtual cards")
public class VirtualCardController {

        private final VirtualCardService virtualCardService;

        public VirtualCardController(VirtualCardService virtualCardService) {
                this.virtualCardService = virtualCardService;
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasPermission('payment:create') and @security.hasValidSession()")
        @Operation(summary = "Create a new virtual card", description = "Creates a new virtual card for the user, charges the applicable issuance fee, and returns the card details.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Card created successfully",
                        content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<VirtualCardResponse> createCard(
                @Valid @RequestBody CreateVirtualCardRequest request) {
                VirtualCardResponse response = virtualCardService.createCard(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/{cardId}")
        @Operation(summary = "Get card by ID", description = "Retrieves a virtual card by its UUID. Card number and CVV are masked — use /details for full sensitive data.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card found",
                        content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> getCardById(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                VirtualCardResponse response = virtualCardService.getCardById(cardId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{cardId}/details")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasValidSession()")
        @Operation(summary = "Get full card details", description = "Returns the unmasked card number, expiry, and CVV. Requires the user to be the card owner and to have a valid session.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card details found",
                        content = @Content(schema = @Schema(implementation = VirtualCardDetailsResponse.class))),
                @ApiResponse(responseCode = "404", description = "Card not found"),
                @ApiResponse(responseCode = "403", description = "Unauthorized access")
        })
        public ResponseEntity<VirtualCardDetailsResponse> getCardDetails(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                VirtualCardDetailsResponse response = virtualCardService.getCardDetails(cardId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
        @Operation(summary = "Get all cards for a user", description = "Returns all virtual cards for the specified user as a flat list.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
        })
        public ResponseEntity<List<VirtualCardResponse>> getCardsByUserId(
                @Parameter(description = "User ID") @PathVariable Long userId) {
                List<VirtualCardResponse> response = virtualCardService.getCardsByUserId(userId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user/{userId}/paginated")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
        @Operation(summary = "Get cards for a user with pagination", description = "Returns virtual cards for the specified user with pagination and sorting support.")
        public ResponseEntity<Page<VirtualCardResponse>> getCardsByUserIdPaginated(
                @Parameter(description = "User ID") @PathVariable Long userId,
                @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
                @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
                Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                
                Page<VirtualCardResponse> response = virtualCardService.getCardsByUserId(userId, pageable);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user/{userId}/active")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
        @Operation(summary = "Get active cards for a user", description = "Returns only ACTIVE (non-frozen, non-cancelled) virtual cards for the specified user.")
        public ResponseEntity<List<VirtualCardResponse>> getActiveCardsByUserId(
                @Parameter(description = "User ID") @PathVariable Long userId) {
                List<VirtualCardResponse> response = virtualCardService.getActiveCardsByUserId(userId);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{cardId}")
        @Operation(summary = "Update card", description = "Updates editable card metadata such as the card label or spending controls.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card updated successfully",
                        content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> updateCard(
                @Parameter(description = "Card ID") @PathVariable String cardId,
                @Valid @RequestBody UpdateVirtualCardRequest request) {
                VirtualCardResponse response = virtualCardService.updateCard(cardId, request);
                return ResponseEntity.ok(response);
        }
        
        @PutMapping("/{cardId}/status")
        @Operation(summary = "Update card status", description = "Transitions the card to a new status. Valid transitions: ACTIVE → FROZEN, FROZEN → ACTIVE, ACTIVE → CANCELLED.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Status updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid status transition"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> updateCardStatus(
                @Parameter(description = "Card ID") @PathVariable String cardId,
                @Parameter(description = "New card status") @RequestBody UpdateCardStatusRequest request) {
                VirtualCardResponse response = virtualCardService.updateCardStatus(cardId, request);
                return ResponseEntity.ok(response);
        }

        @PatchMapping("/{cardId}/balance")
        @Operation(summary = "Update card balance", description = "Credits or debits the card balance. Use BalanceOperation.ADD to top-up and BalanceOperation.DEDUCT to deduct funds.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Balance updated successfully"),
                @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid operation"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> updateBalance(
                @Parameter(description = "Card ID") @PathVariable String cardId,
                @Valid @RequestBody UpdateBalanceRequest request) {
                VirtualCardResponse response = virtualCardService.updateBalance(cardId, request);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{cardId}")
        @Operation(summary = "Delete card (soft delete)", description = "Marks the card as deleted and prevents all future transactions. The card record is retained for audit purposes.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<Void> deleteCard(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                virtualCardService.deleteCard(cardId);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{cardId}/freeze")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasValidSession()")
        @Operation(summary = "Freeze card", description = "Suspends a card temporarily. All transactions will be declined until the card is unfrozen.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card frozen successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid operation"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> freezeCard(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                VirtualCardResponse response = virtualCardService.freezeCard(cardId);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{cardId}/unfreeze")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasValidSession()")
        @Operation(summary = "Unfreeze card", description = "Restores a frozen card to ACTIVE status so transactions are permitted again.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card unfrozen successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid operation"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> unfreezeCard(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                VirtualCardResponse response = virtualCardService.unfreezeCard(cardId);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{cardId}/cancel")
        @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasValidSession()")
        @Operation(summary = "Cancel card", description = "Permanently cancels a virtual card. This action cannot be reversed — the user must create a new card.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Card cancelled successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid operation"),
                @ApiResponse(responseCode = "404", description = "Card not found")
        })
        public ResponseEntity<VirtualCardResponse> cancelCard(
                @Parameter(description = "Card ID") @PathVariable String cardId) {
                VirtualCardResponse response = virtualCardService.cancelCard(cardId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/count")
        @Operation(summary = "Get total virtual cards count", description = "Returns the total number of non-deleted virtual cards across the entire platform.")
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
        public ResponseEntity<Long> getTotalCards() {
                return ResponseEntity.ok(virtualCardService.getTotalCards());
        }

}
