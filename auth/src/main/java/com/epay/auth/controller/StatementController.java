package com.epay.auth.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.epay.auth.interfaces.IStatementService;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.domain.auth.input.StatementRequest;

@RestController
@RequestMapping("/receipt")
public class StatementController {

    private final IStatementService pdfService;

    public StatementController(IStatementService pdfService) {
        this.pdfService = pdfService;
    }
    
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/generate-pdf/{userId}")
    public ResponseEntity<?> generatePDF(
            @PathVariable("userId") Long userId,
            @RequestBody StatementRequest request) {
        try {
            pdfService.generateAndSendBankStatement(
                request.getEmail(), "Customer", request.getStatements()
            );
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "PDF generated and sent successfully");
            response.put("email", request.getEmail());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            e.printStackTrace();
            throw new BadRequestException(
                "Error generating/sending PDF, " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), 
                ErrorCode.INVALID_INPUT
            );
        }
    }

}
