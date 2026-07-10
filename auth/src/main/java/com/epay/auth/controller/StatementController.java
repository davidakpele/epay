package com.epay.auth.controller;

import com.epay.auth.interfaces.IStatementService;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.auth.input.StatementRequest;
import com.epay.domain.auth.repository.UserRecordRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/receipt")
@RequiredArgsConstructor
public class StatementController {

    private final IStatementService  statementService;
    private final UserRecordRepository userRecordRepository;

    /**
     * POST /receipt/generate-pdf/{userId}
     * Generates a PDF bank statement and emails it to the user.
     * userId is injected from JWT via @RequestAttribute.
     */
    @PostMapping("/generate-pdf")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generatePDF(
            @Valid @RequestBody StatementRequest request,
            @RequestAttribute("userId") Long userId) {

        if (request.getStatements() == null || request.getStatements().isEmpty())
            throw new BadRequestException("Statement data is required", ErrorCode.INVALID_INPUT);

        // Resolve full name from auth module — fall back to username from request
        String fullName = userRecordRepository.findByUserId(userId)
                .map(r -> r.getFirstName() + " " + r.getLastName())
                .orElse(request.getUsername() != null ? request.getUsername() : "Customer");

        String email = request.getEmail();

        try {
            statementService.generateAndSendBankStatement(email, fullName, request.getStatements());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success",   true);
            response.put("message",   "Account statement generated and sent to " + email);
            response.put("email",     email);
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new BadRequestException(
                    "Failed to generate statement: " + cause.getMessage(),
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
