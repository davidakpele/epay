package pesco.example.virtual_card_service.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pesco.example.virtual_card_service.requests.CreateVirtualCardRequest;
import pesco.example.virtual_card_service.requests.UpdateBalanceRequest;
import pesco.example.virtual_card_service.requests.UpdateCardStatusRequest;
import pesco.example.virtual_card_service.requests.UpdateVirtualCardRequest;
import pesco.example.virtual_card_service.responses.VirtualCardDetailsResponse;
import pesco.example.virtual_card_service.responses.VirtualCardResponse;
import pesco.example.virtual_card_service.services.VirtualCardService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/virtual-cards")
@RequiredArgsConstructor
@Tag(name = "Virtual Card Management", description = "APIs for managing virtual cards")
public class VirtualCardController {

    private final VirtualCardService virtualCardService;

    // ==================== CREATE ====================
    @PostMapping
    @Operation(summary = "Create a new virtual card", description = "Creates a new virtual card for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<VirtualCardResponse> createCard(
            @Valid @RequestBody CreateVirtualCardRequest request) {
        log.info("REST request to create virtual card for user: {}", request.getUserId());
        VirtualCardResponse response = virtualCardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== READ ====================
    @GetMapping("/{cardId}")
    @Operation(summary = "Get card by ID", description = "Retrieves a virtual card by its ID (masked data)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found",
                    content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> getCardById(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to get virtual card: {}", cardId);
        VirtualCardResponse response = virtualCardService.getCardById(cardId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}/details")
    @Operation(summary = "Get full card details", 
               description = "Retrieves full card details including sensitive data (requires authorization)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card details found",
                    content = @Content(schema = @Schema(implementation = VirtualCardDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<VirtualCardDetailsResponse> getCardDetails(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to get virtual card details: {}", cardId);
        VirtualCardDetailsResponse response = virtualCardService.getCardDetails(cardId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all cards for a user", description = "Retrieves all virtual cards for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully")
    })
    public ResponseEntity<List<VirtualCardResponse>> getCardsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("REST request to get all cards for user: {}", userId);
        List<VirtualCardResponse> response = virtualCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/paginated")
    @Operation(summary = "Get cards for a user with pagination", 
               description = "Retrieves virtual cards for a user with pagination support")
    public ResponseEntity<Page<VirtualCardResponse>> getCardsByUserIdPaginated(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("REST request to get cards for user: {} with pagination", userId);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<VirtualCardResponse> response = virtualCardService.getCardsByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active cards for a user", 
               description = "Retrieves only active virtual cards for a specific user")
    public ResponseEntity<List<VirtualCardResponse>> getActiveCardsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("REST request to get active cards for user: {}", userId);
        List<VirtualCardResponse> response = virtualCardService.getActiveCardsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================
    @PutMapping("/{cardId}")
    @Operation(summary = "Update card", description = "Updates a virtual card's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully",
                    content = @Content(schema = @Schema(implementation = VirtualCardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> updateCard(
            @Parameter(description = "Card ID") @PathVariable String cardId,
            @Valid @RequestBody UpdateVirtualCardRequest request) {
        log.info("REST request to update virtual card: {}", cardId);
        VirtualCardResponse response = virtualCardService.updateCard(cardId, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{cardId}/status")
    @Operation(summary = "Update card status", description = "Updates the status of a virtual card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> updateCardStatus(
            @Parameter(description = "Card ID") @PathVariable String cardId,
            @Valid @RequestBody UpdateCardStatusRequest request) {
        log.info("REST request to update card status: {} to {}", cardId, request.getStatus());
        VirtualCardResponse response = virtualCardService.updateCardStatus(cardId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/balance")
    @Operation(summary = "Update card balance", description = "Adds or deducts balance from a virtual card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance updated successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid operation"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> updateBalance(
            @Parameter(description = "Card ID") @PathVariable String cardId,
            @Valid @RequestBody UpdateBalanceRequest request) {
        log.info("REST request to update balance for card: {}", cardId);
        VirtualCardResponse response = virtualCardService.updateBalance(cardId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete card (soft delete)", 
               description = "Soft deletes a virtual card by marking it as deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to soft delete virtual card: {}", cardId);
        virtualCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cardId}/permanent")
    @Operation(summary = "Permanently delete card", 
               description = "Permanently deletes a virtual card from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card permanently deleted"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> permanentlyDeleteCard(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to permanently delete virtual card: {}", cardId);
        virtualCardService.permanentlyDeleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    // ==================== CARD OPERATIONS ====================
    @PostMapping("/{cardId}/freeze")
    @Operation(summary = "Freeze card", description = "Freezes a virtual card to prevent transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card frozen successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> freezeCard(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to freeze card: {}", cardId);
        VirtualCardResponse response = virtualCardService.freezeCard(cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardId}/unfreeze")
    @Operation(summary = "Unfreeze card", description = "Unfreezes a virtual card to allow transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card unfrozen successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> unfreezeCard(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to unfreeze card: {}", cardId);
        VirtualCardResponse response = virtualCardService.unfreezeCard(cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardId}/cancel")
    @Operation(summary = "Cancel card", description = "Cancels a virtual card permanently")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<VirtualCardResponse> cancelCard(
            @Parameter(description = "Card ID") @PathVariable String cardId) {
        log.info("REST request to cancel card: {}", cardId);
        VirtualCardResponse response = virtualCardService.cancelCard(cardId);
        return ResponseEntity.ok(response);
    }

    // ==================== HEALTH CHECK ====================
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the virtual card service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Virtual Card Service is running");
    }
}