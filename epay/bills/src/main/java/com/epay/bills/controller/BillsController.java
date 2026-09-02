package com.epay.bills.controller;

import com.epay.domain.bills.dto.BillPaymentResponse;
import com.epay.domain.bills.input.*;
import com.epay.bills.service.BillsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "Bill payment APIs — airtime, data, cable TV, electricity, betting, shopping")
public class BillsController {

    private final BillsService billsService;

    @PostMapping("/airtime")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Purchase airtime",
        description = "Deducts the specified amount from the user's wallet and tops up the given phone number with airtime."
    )
    public ResponseEntity<BillPaymentResponse> airtime(
            @Valid @RequestBody AirtimeRequest request) {
        return billsService.payAirtime(request);
    }

    @PostMapping("/data")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Purchase data bundle",
        description = "Deducts the specified amount and activates a data bundle on the given phone number."
    )
    public ResponseEntity<BillPaymentResponse> data(
            @Valid @RequestBody DataRequest request) {
        return billsService.payData(request);
    }

    @PostMapping("/cabletv")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Pay cable TV subscription",
        description = "Activates or renews a DSTV / GOTV / STARTIMES subscription for the given smart card."
    )
    public ResponseEntity<BillPaymentResponse> cableTv(
            @Valid @RequestBody CableTvRequest request) {
        return billsService.payCableTv(request);
    }

    @PostMapping("/electricity")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Pay electricity bill",
        description = "Purchases a prepaid token or settles a postpaid bill for the given meter number."
    )
    public ResponseEntity<BillPaymentResponse> electricity(
            @Valid @RequestBody ElectricityRequest request) {
        return billsService.payElectricity(request);
    }

    @PostMapping("/betting")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Fund betting wallet",
        description = "Tops up a user's betting account on the specified platform."
    )
    public ResponseEntity<BillPaymentResponse> betting(
            @Valid @RequestBody BettingRequest request) {
        return billsService.payBetting(request);
    }

    @PostMapping("/shopping")
    @PreAuthorize("hasRole('USER') and @security.hasPermission('payment:create') and @security.hasValidSession()")
    @Operation(
        summary     = "Pay shopping order",
        description = "Processes payment for a shopping order from the specified platform."
    )
    public ResponseEntity<BillPaymentResponse> shopping(
            @Valid @RequestBody ShoppingRequest request) {
        return billsService.payShopping(request);
    }
}