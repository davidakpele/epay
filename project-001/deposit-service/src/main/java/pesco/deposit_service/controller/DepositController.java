package pesco.deposit_service.controller;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pesco.deposit_service.services.DepositService;
import pesco.deposit_service.enums.TransactionType;
import pesco.deposit_service.exceptions.Error;
import pesco.deposit_service.payloads.DepositRequest;

@RestController
@RequestMapping("/deposit")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDeposit(@RequestBody DepositRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        if (token.isBlank() || token.isEmpty()) {
            return Error.createResponse("UNAUTHORIZED*.",
                    HttpStatus.UNAUTHORIZED, "Require token to access this endpoint, Missing valid token.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Error.createResponse("Amount is required and must be greater than zero.", HttpStatus.BAD_REQUEST,
                    "Please provide a valid amount you want to deposit.");
        }

        if (request.getCurrencyType() == null || request.getCurrencyType().toString().isEmpty()) {
            return Error.createResponse("Currency type is require.*", HttpStatus.BAD_REQUEST,
                    "Please provide the currency type you want deposit, e.g 'USD OR NGN'");
        }

        if (request.getType().equals(TransactionType.DEPOSIT)) {
            return depositService.createDeposit(request, token);
        }

        return Error.createResponse("Wrong Transaction format.", HttpStatus.BAD_REQUEST,
                "please change the transaction Type to Deposit");
    }

}