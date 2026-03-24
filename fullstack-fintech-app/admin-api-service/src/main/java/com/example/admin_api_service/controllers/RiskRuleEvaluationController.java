package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IRiskRuleEvaluationService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.RiskRuleEvaluationOutcome;
import com.example.admin_api_service.models.complianceAndRisk.RiskRuleEvaluation;
import com.example.admin_api_service.payloads.EvaluateTransactionPayload;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/risk-evaluations")
public class RiskRuleEvaluationController {

    private final IRiskRuleEvaluationService evaluationService;

    public RiskRuleEvaluationController(IRiskRuleEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<List<RiskRuleEvaluation>>> evaluateTransaction(
            @Valid @RequestBody EvaluateTransactionPayload payload) {

        List<RiskRuleEvaluation> results = evaluationService.evaluateTransaction(
                payload.getUserId(),
                payload.getWalletId(),
                payload.getTransactionId(),
                payload.getInputContextJson()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(results, "Transaction evaluated against all active rules"));
    }

    @PostMapping("/evaluate/rule/{ruleCode}")
    public ResponseEntity<ApiResponse<RiskRuleEvaluation>> evaluateSingleRule(
            @PathVariable String ruleCode,
            @Valid @RequestBody EvaluateTransactionPayload payload) {

        RiskRuleEvaluation result = evaluationService.evaluateSingleRule(
                ruleCode,
                payload.getUserId(),
                payload.getWalletId(),
                payload.getTransactionId(),
                payload.getInputContextJson()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Rule evaluated"));
    }

    @GetMapping("/{evaluationId}")
    public ResponseEntity<ApiResponse<RiskRuleEvaluation>> getEvaluationById(
            @PathVariable String evaluationId) {
        return ResponseEntity.ok(ApiResponse.success(
                evaluationService.getEvaluationById(evaluationId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RiskRuleEvaluation>>> getEvaluationsByOutcome(
            @RequestParam(required = false) RiskRuleEvaluationOutcome outcome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("evaluatedAt").descending());
        Page<RiskRuleEvaluation> evaluations = outcome != null
                ? evaluationService.getEvaluationsByOutcome(outcome, pageable)
                : evaluationService.getEvaluationsByOutcome(null, pageable);

        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<RiskRuleEvaluation>>> getEvaluationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                evaluationService.getEvaluationsByUser(
                        userId, PageRequest.of(page, size, Sort.by("evaluatedAt").descending()))));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<Page<RiskRuleEvaluation>>> getEvaluationsByTransaction(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                evaluationService.getEvaluationsByTransaction(
                        transactionId, PageRequest.of(page, size, Sort.by("evaluatedAt").descending()))));
    }

    @GetMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<Page<RiskRuleEvaluation>>> getEvaluationsByRule(
            @PathVariable String ruleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                evaluationService.getEvaluationsByRule(
                        ruleId, PageRequest.of(page, size, Sort.by("evaluatedAt").descending()))));
    }

    @GetMapping("/users/{userId}/risk-score")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserRiskScore(
            @PathVariable Long userId) {

        BigDecimal score = evaluationService.calculateUserRiskScore(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "userId", userId,
                "riskScore", score
        )));
    }
}