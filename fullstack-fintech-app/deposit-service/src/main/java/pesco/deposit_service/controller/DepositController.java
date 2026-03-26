package pesco.deposit_service.controller;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import pesco.deposit_service.services.DepositService;
import pesco.deposit_service.services.GeoLocationService;
import pesco.deposit_service.components.IpExtractor;
import pesco.deposit_service.enums.TransactionType;
import pesco.deposit_service.exceptions.Error;
import pesco.deposit_service.payloads.DepositRequest;

@RestController
@RequestMapping("/deposit")
public class DepositController {

    private final DepositService depositService;
    private final IpExtractor ipExtractor;
    private final GeoLocationService geoLocationService;
    
    public DepositController(DepositService depositService, IpExtractor ipExtractor, GeoLocationService geoLocationService) {
        this.depositService = depositService;
        this.ipExtractor = ipExtractor;
        this.geoLocationService = geoLocationService;
    }


    @PostMapping("/create")
    public ResponseEntity<?> createDeposit(
            @RequestBody DepositRequest request,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader(value = "User-Agent",      required = false) String userAgent,
            @RequestHeader(value = "X-Geo-Location",  required = false) String geoLocation,
            @RequestHeader(value = "X-Device-Id",     required = false) String deviceId,
            HttpServletRequest httpRequest) {

        String token = authorizationHeader.replace("Bearer ", "").trim();
        if (token.isBlank()) {
            return Error.createResponse("UNAUTHORIZED",
                    HttpStatus.UNAUTHORIZED,
                    "Missing valid token.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Error.createResponse("Invalid amount.",
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero.");
        }

        if (request.getCurrencyType() == null || request.getCurrencyType().toString().isBlank()) {
            return Error.createResponse("Currency type is required.",
                    HttpStatus.BAD_REQUEST,
                    "Please provide a currency type e.g. USD, NGN.");
        }

        // ── Enrich request with device/location context ──────────────
        String ipAddress      = ipExtractor.extract(httpRequest);
        String resolvedGeo    = geoLocationService.resolve(geoLocation, ipAddress);

        request.setIpAddress(ipAddress);
        request.setUserAgent(userAgent);
        request.setDeviceId(deviceId);
        request.setGeoLocation(resolvedGeo);

        if (!TransactionType.DEPOSIT.equals(request.getType())) {
            return Error.createResponse("Wrong transaction type.",
                    HttpStatus.BAD_REQUEST,
                    "Transaction type must be DEPOSIT.");
        }

        return depositService.createDeposit(request, token);
    }

}