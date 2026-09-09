package com.epay.investment.controller;

import com.epay.common.interfaces.InvestmentServices;
import com.epay.domain.investment.dto.InvestmentReturnResponse;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.input.CalculateInvestmentRequest;
import com.epay.domain.investment.input.CreateInvestmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Investments", description = "Fixed-term investment plans — browse, calculate, and create")
@Slf4j
@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentServices investmentService;

    @Operation(
        summary     = "List available investment plans",
        description = "Returns all fixed-term investment durations (WEEKLY, MONTHLY, QUARTERLY, YEARLY) with their annual return rates."
    )
    @GetMapping("/plans")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getPlans() {
        List<InvestmentReturnResponse> plans = List.of(
                InvestmentReturnResponse.builder().duration("WEEKLY").durationDays(7).annualRate(BigDecimal.valueOf(8)).build(),
                InvestmentReturnResponse.builder().duration("MONTHLY").durationDays(30).annualRate(BigDecimal.valueOf(12)).build(),
                InvestmentReturnResponse.builder().duration("QUARTERLY").durationDays(90).annualRate(BigDecimal.valueOf(18)).build(),
                InvestmentReturnResponse.builder().duration("YEARLY").durationDays(365).annualRate(BigDecimal.valueOf(24)).build()
        );
        return ResponseEntity.ok(Map.of("status", "success", "data", plans));
    }

    @Operation(
        summary     = "Calculate investment returns",
        description = "Previews the expected profit and total payout for a given principal, duration, and currency before committing."
    )
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> calculate(@Valid @RequestBody CalculateInvestmentRequest request) {
        try {
            InvestmentReturnResponse response = investmentService.calculateReturns(
                    request.getPrincipal(), request.getDuration(), request.getCurrencyCode());
            return ResponseEntity.ok(Map.of("status", "success", "data", response));
        } catch (Exception ex) {
            log.error("Investment calculation failed", ex);
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "failed", "message", ex.getMessage()));
        }
    }

    @Operation(
        summary     = "Create an investment",
        description = "Locks the specified principal from the user's wallet into a fixed-term investment plan. Requires payment:create permission and a valid session."
    )
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> create(@Valid @RequestBody CreateInvestmentRequest request) {
        try {
            Investment investment = investmentService.createInvestment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("status", "success", "message", "Investment created successfully.", "data", investment));
        } catch (Exception ex) {
            log.error("Investment creation failed", ex);
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "failed", "message", ex.getMessage()));
        }
    }

    @Operation(
        summary     = "List investments for a user",
        description = "Returns all investment records for the specified user. Users can only see their own investments."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
    public ResponseEntity<?> getByUserId(@PathVariable Long userId) {
        try {
            List<Investment> investments = investmentService.getByUserId(userId);
            return ResponseEntity.ok(Map.of("status", "success", "data", investments));
        } catch (Exception ex) {
            log.error("Failed to fetch investments", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "failed", "message", "Unable to retrieve investments."));
        }
    }

    @Operation(
        summary     = "Get a single investment by ID",
        description = "Returns the full details of an investment plan including status, returns, and maturity date."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
    public ResponseEntity<?> getById(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Investment investment = investmentService.getById(id, userId);
            if (investment == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "failed", "message", "Investment not found."));
            return ResponseEntity.ok(Map.of("status", "success", "data", investment));
        } catch (Exception ex) {
            log.error("Failed to fetch investment", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "failed", "message", "Unable to retrieve investment."));
        }
    }
}
