package com.epay.investment.controller;

import com.epay.common.interfaces.InvestmentServices;
import com.epay.domain.investment.dto.InvestmentReturnResponse;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.input.CalculateInvestmentRequest;
import com.epay.domain.investment.input.CreateInvestmentRequest;
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

@Slf4j
@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentServices investmentService;

    @GetMapping("/plans")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getPlans() {

        List<InvestmentReturnResponse> plans = List.of(

                InvestmentReturnResponse.builder()
                        .duration("WEEKLY")
                        .durationDays(7)
                        .annualRate(BigDecimal.valueOf(8))
                        .build(),

                InvestmentReturnResponse.builder()
                        .duration("MONTHLY")
                        .durationDays(30)
                        .annualRate(BigDecimal.valueOf(12))
                        .build(),

                InvestmentReturnResponse.builder()
                        .duration("QUARTERLY")
                        .durationDays(90)
                        .annualRate(BigDecimal.valueOf(18))
                        .build(),

                InvestmentReturnResponse.builder()
                        .duration("YEARLY")
                        .durationDays(365)
                        .annualRate(BigDecimal.valueOf(24))
                        .build()
        );

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "data", plans
                )
        );
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> calculate(
            @Valid @RequestBody CalculateInvestmentRequest request) {

        try {

            InvestmentReturnResponse response =
                    investmentService.calculateReturns(
                            request.getPrincipal(),
                            request.getDuration(),
                            request.getCurrencyCode());

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "data", response
                    )
            );

        } catch (Exception ex) {

            log.error("Investment calculation failed", ex);

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "failed",
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    public ResponseEntity<?> create(
            @Valid @RequestBody CreateInvestmentRequest request) {

        try {

            Investment investment =
                    investmentService.createInvestment(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(
                            Map.of(
                                    "status", "success",
                                    "message", "Investment created successfully.",
                                    "data", investment
                            )
                    );

        } catch (Exception ex) {

            log.error("Investment creation failed", ex);

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "failed",
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
    public ResponseEntity<?> getByUserId(
            @PathVariable Long userId) {

        try {

            List<Investment> investments =
                    investmentService.getByUserId(userId);

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "data", investments
                    )
            );

        } catch (Exception ex) {

            log.error("Failed to fetch investments", ex);

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "status", "failed",
                            "message", "Unable to retrieve investments."
                    )
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN') and @security.isOwner(#userId)")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {

            Investment investment =
                    investmentService.getById(id, userId);

            if (investment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                Map.of(
                                        "status", "failed",
                                        "message", "Investment not found."
                                )
                        );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "data", investment
                    )
            );

        } catch (Exception ex) {

            log.error("Failed to fetch investment", ex);

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "status", "failed",
                            "message", "Unable to retrieve investment."
                    )
            );
        }
    }
}