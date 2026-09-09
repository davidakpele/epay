package com.epay.auth.controller;

import com.epay.auth.interfaces.IStatementService;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.auth.input.StatementRequest;
import com.epay.domain.auth.repository.UserRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Account Statement", description = "Generate and email PDF account statements")
@RestController
@RequestMapping("/receipt")
@RequiredArgsConstructor
public class StatementController {

    private final IStatementService    statementService;
    private final UserRecordRepository userRecordRepository;

    @Operation(
        summary     = "Generate and email a PDF account statement",
        description = "Renders the supplied transaction list into a PDF statement and sends it to the user's registered email address."
    )
    @PostMapping("/generate-pdf/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generatePDF(
            @Valid @RequestBody StatementRequest request,
            @PathVariable Long userId) {

        if (request.getStatements() == null || request.getStatements().isEmpty())
            throw new BadRequestException("Statement data is required", ErrorCode.INVALID_INPUT);

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
