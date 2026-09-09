package com.epay.admin.controller;

import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.maintenance.dto.*;
import com.epay.domain.maintenance.entity.MaintenanceFeeTransaction;
import com.epay.domain.maintenance.entity.UserDebt;
import com.epay.domain.maintenance.entity.UserMonthlyActivity;
import com.epay.domain.maintenance.enums.DebtStatus;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import com.epay.domain.maintenance.input.CreateFeeConfigRequest;
import com.epay.domain.maintenance.input.WaiveFeeRequest;
import com.epay.maintenance.repository.MaintenanceFeeAuditLogRepository;
import com.epay.maintenance.repository.MaintenanceFeeTransactionRepository;
import com.epay.maintenance.repository.UserDebtRepository;
import com.epay.maintenance.repository.UserMonthlyActivityRepository;
import com.epay.maintenance.scheduler.MaintenanceFeeScheduler;
import com.epay.maintenance.service.MaintenanceAdminService;
import com.epay.maintenance.service.MaintenanceAdminService.*;
import com.epay.maintenance.service.MaintenanceFeeConfigService;
import com.epay.maintenance.service.MaintenanceFeeEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Maintenance Fee Management API
 *
 * Base path: /admin/maintenance
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SECTION            │  PATH                        │  WHO               │
 * ├─────────────────────┼──────────────────────────────┼────────────────────┤
 * │  Dashboard          │  /overview                   │  ADMIN, SUPER_USER │
 * │  Fee Config         │  /fee-configs/**             │  ADMIN, SUPER_USER │
 * │  Fee Transactions   │  /transactions/**            │  ADMIN+, CS        │
 * │  Debt Ledger        │  /debts/**                   │  ADMIN+, CS        │
 * │  Debt Aging Report  │  /debts/aging                │  ADMIN, SUPER_USER │
 * │  User Profile       │  /users/{userId}/profile     │  ADMIN+, CS        │
 * │  Monthly Reports    │  /reports/month/**           │  ADMIN+, CS        │
 * │  Activity           │  /activity/**                │  ADMIN+, CS        │
 * │  Audit Logs         │  /audit/**                   │  ADMIN, SUPER_USER │
 * │  Batch Control      │  /batch/**                   │  SUPER_USER only   │
 * └─────────────────────┴──────────────────────────────┴────────────────────┘
 */
@Tag(name = "Admin — Maintenance Fees", description = "Maintenance fee configuration, history, debts and reports")
@RestController
@RequestMapping("/admin/maintenance")
@RequiredArgsConstructor
public class AdminMaintenanceFeeController {

    private final MaintenanceFeeConfigService         configService;
    private final MaintenanceFeeEngine                feeEngine;
    private final MaintenanceFeeScheduler             scheduler;
    private final MaintenanceAdminService             adminService;
    private final MaintenanceFeeTransactionRepository feeTransactionRepo;
    private final UserDebtRepository                  debtRepo;
    private final UserMonthlyActivityRepository       activityRepo;
    private final MaintenanceFeeAuditLogRepository    auditLogRepo;
    private final JwtClaimsHolder                     jwtClaims;

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD OVERVIEW
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/overview
     * GET /admin/maintenance/overview?billingMonth=2026-09-01
     *
     * Returns a complete dashboard snapshot:
     *   - Per-status counts for the billing month (DEDUCTED, PARTIAL, DEBT, FAILED, etc.)
     *   - Total amount deducted vs outstanding debt for the month
     *   - Number of users currently in debt across the platform
     *   - All-time outstanding debt totalled per currency
     *   - Debt ledger status distribution (ACTIVE / PARTIAL / SETTLED)
     *   - List of all available billing months (for the month picker dropdown)
     *   - All active fee configurations
     *
     * Defaults to the previous calendar month if billingMonth is omitted.
     */
    @Operation(summary = "Dashboard overview — fee stats for a billing month")
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> overview(
            @Parameter(description = "Billing month (YYYY-MM-01). Defaults to last month.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth) {
        return ResponseEntity.ok(ApiResponse.success(null,
                adminService.getDashboardOverview(billingMonth)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FEE CONFIGURATION — READ, CREATE, UPDATE, DEACTIVATE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/fee-configs
     * All currently active fee configurations across all currencies.
     */
    @Operation(summary = "List all active fee configurations")
    @GetMapping("/fee-configs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<MaintenanceFeeConfigDto>>> listActiveConfigs() {
        return ResponseEntity.ok(ApiResponse.success(null, configService.getAllActive()));
    }

    /**
     * GET /admin/maintenance/fee-configs/{currency}
     * All configurations (active + historic) for a currency.
     */
    @Operation(summary = "All configs for a currency (including history)")
    @GetMapping("/fee-configs/{currency}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<MaintenanceFeeConfigDto>>> configsForCurrency(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(null,
                configService.getAllForCurrency(currency)));
    }

    /**
     * GET /admin/maintenance/fee-configs/{currency}/active
     * The single currently active config for a currency.
     */
    @Operation(summary = "Current active config for a specific currency")
    @GetMapping("/fee-configs/{currency}/active")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<MaintenanceFeeConfigDto>> activeConfigForCurrency(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(null,
                configService.getActiveForCurrency(currency)));
    }

    /**
     * GET /admin/maintenance/fee-configs/id/{id}
     * A single config by its database ID.
     */
    @Operation(summary = "Get a single fee config by ID")
    @GetMapping("/fee-configs/id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<MaintenanceFeeConfigDto>> configById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null, configService.getById(id)));
    }

    /**
     * POST /admin/maintenance/fee-configs
     * Creates a new active fee config for a currency.
     * Any existing active config for that currency is automatically deactivated.
     *
     * Body — FIXED fee example:
     * { "currencyCode": "USD", "feeType": "FIXED", "feeAmount": 1.50 }
     *
     * Body — PERCENTAGE fee example:
     * { "currencyCode": "NGN", "feeType": "PERCENTAGE", "feePercentage": 0.10,
     *   "minimumFee": 50.00, "maximumFee": 500.00 }
     */
    @Operation(summary = "Create / replace fee config for a currency")
    @PostMapping("/fee-configs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<MaintenanceFeeConfigDto>> createConfig(
            @Valid @RequestBody CreateFeeConfigRequest request) {
        MaintenanceFeeConfigDto created = configService.createOrReplace(request, jwtClaims.getUserId());
        return ResponseEntity.status(201).body(
                ApiResponse.success("Fee configuration saved", created));
    }

    /**
     * PUT /admin/maintenance/fee-configs/{currency}
     * Updates (replaces) the active config for a currency.
     * Semantically identical to POST — kept for REST convention.
     */
    @Operation(summary = "Update fee config for a currency (replaces existing)")
    @PutMapping("/fee-configs/{currency}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<MaintenanceFeeConfigDto>> updateConfig(
            @PathVariable String currency,
            @Valid @RequestBody CreateFeeConfigRequest request) {
        // Ensure currency in path matches body
        request.setCurrencyCode(currency.toUpperCase());
        MaintenanceFeeConfigDto updated = configService.createOrReplace(request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Fee configuration updated", updated));
    }

    /**
     * DELETE /admin/maintenance/fee-configs/{id}
     * Soft-deactivates a config by database ID.
     * The record is kept for audit; fees won't be charged for this currency until
     * a new config is created.
     */
    @Operation(summary = "Deactivate a fee config by ID")
    @DeleteMapping("/fee-configs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> deactivateConfig(@PathVariable Long id) {
        configService.deactivate(id, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Fee configuration deactivated", null));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FEE TRANSACTIONS — CHARGE HISTORY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/transactions
     * GET /admin/maintenance/transactions?status=DEBT
     *
     * All fee transactions, optionally filtered by status.
     * Statuses: PENDING, DEDUCTED, DEBT, PARTIAL, REPAID, WAIVED, FAILED
     */
    @Operation(summary = "List all fee transactions, optionally filtered by status")
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<MaintenanceFeeTransactionDto>>> listTransactions(
            @RequestParam(required = false) MaintenanceFeeStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<MaintenanceFeeTransaction> page = (status != null)
                ? feeTransactionRepo.findByStatus(status, pageable)
                : feeTransactionRepo.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page.map(adminService::toFeeDto)));
    }

    /**
     * GET /admin/maintenance/transactions/{id}
     * Single fee transaction by database ID.
     */
    @Operation(summary = "Get a single fee transaction by ID")
    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<MaintenanceFeeTransactionDto>> getTransaction(@PathVariable Long id) {
        MaintenanceFeeTransaction t = feeTransactionRepo.findById(id)
                .orElseThrow(() -> new com.epay.common.exception.ResourceNotFoundException(
                        "Fee transaction not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(null, adminService.toFeeDto(t)));
    }

    /**
     * GET /admin/maintenance/transactions/reference/{referenceId}
     * Lookup by reference ID (the MAINT-XXXXX string on the transaction).
     */
    @Operation(summary = "Find transaction by reference ID")
    @GetMapping("/transactions/reference/{referenceId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<MaintenanceFeeTransactionDto>> getByReference(
            @PathVariable String referenceId) {
        MaintenanceFeeTransaction t = feeTransactionRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new com.epay.common.exception.ResourceNotFoundException(
                        "Transaction not found for reference: " + referenceId));
        return ResponseEntity.ok(ApiResponse.success(null, adminService.toFeeDto(t)));
    }

    /**
     * GET /admin/maintenance/transactions/month/{monthYear}
     * All transactions for a specific billing month.
     * monthYear format: YYYY-MM-01  (e.g. 2026-09-01 for September 2026)
     */
    @Operation(summary = "All transactions for a billing month")
    @GetMapping("/transactions/month/{monthYear}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<MaintenanceFeeTransactionDto>>> transactionsByMonth(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthYear,
            @RequestParam(required = false) MaintenanceFeeStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MaintenanceFeeTransaction> page = (status != null)
                ? feeTransactionRepo.findByMonthAndStatus(monthYear.withDayOfMonth(1), status, pageable)
                : feeTransactionRepo.findByMonth(monthYear.withDayOfMonth(1), pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page.map(adminService::toFeeDto)));
    }

    /**
     * GET /admin/maintenance/transactions/user/{userId}
     * All fee transactions for a specific user, newest first.
     */
    @Operation(summary = "All fee transactions for a user")
    @GetMapping("/transactions/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<MaintenanceFeeTransactionDto>>> transactionsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                feeTransactionRepo.findByUserId(userId, pageable).map(adminService::toFeeDto)));
    }

    /**
     * GET /admin/maintenance/transactions/user/{userId}/month/{monthYear}
     * A specific user's transactions for a specific month.
     */
    @Operation(summary = "User transactions for a specific billing month")
    @GetMapping("/transactions/user/{userId}/month/{monthYear}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<List<MaintenanceFeeTransactionDto>>> userTransactionsForMonth(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthYear) {
        List<MaintenanceFeeTransactionDto> list =
                feeTransactionRepo.findByUserIdAndMonth(userId, monthYear.withDayOfMonth(1))
                        .stream().map(adminService::toFeeDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, list));
    }

    /**
     * POST /admin/maintenance/transactions/waive
     * Manually waives a fee transaction (PENDING, DEBT, or PARTIAL).
     * Clears the corresponding debt from the user's debt ledger.
     *
     * Body: { "feeTransactionId": 123, "reason": "Customer goodwill gesture" }
     */
    @Operation(summary = "Waive a maintenance fee transaction")
    @PostMapping("/transactions/waive")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> waiveFee(@Valid @RequestBody WaiveFeeRequest request) {
        feeEngine.waive(request, jwtClaims.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Fee transaction waived", null));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEBT LEDGER
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/debts
     * GET /admin/maintenance/debts?status=ACTIVE
     *
     * All debt ledger entries.
     * statuses: ACTIVE, PARTIAL, SETTLED
     */
    @Operation(summary = "List all debt records, optionally filtered by status")
    @GetMapping("/debts")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<UserDebtDto>>> listDebts(
            @RequestParam(required = false) DebtStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDebt> page = (status != null)
                ? debtRepo.findByStatus(status, pageable)
                : debtRepo.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page.map(adminService::toDebtDto)));
    }

    /**
     * GET /admin/maintenance/debts/summary
     * Total outstanding debt per currency — the admin finance dashboard widget.
     */
    @Operation(summary = "Outstanding debt totals per currency")
    @GetMapping("/debts/summary")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> debtSummary() {
        List<Map<String, Object>> result = debtRepo.sumActiveDebtByCurrency().stream()
                .map(r -> Map.<String, Object>of("currency", r[0], "totalOutstandingDebt", r[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, result));
    }

    /**
     * GET /admin/maintenance/debts/aging
     * Platform-wide debt aging report split into 4 brackets:
     *   0–30 days | 31–60 days | 61–90 days | 90+ days
     *
     * Useful for knowing which debts need escalation to collections.
     */
    @Operation(summary = "Debt aging report — categorised by age brackets")
    @GetMapping("/debts/aging")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<DebtAgingReportDto>> debtAgingReport() {
        return ResponseEntity.ok(ApiResponse.success(null, adminService.getDebtAgingReport()));
    }

    /**
     * GET /admin/maintenance/debts/user/{userId}
     * All active debts for a specific user (ACTIVE or PARTIAL only).
     */
    @Operation(summary = "All active debts for a user")
    @GetMapping("/debts/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<List<UserDebtDto>>> userDebts(@PathVariable Long userId) {
        List<UserDebtDto> debts = debtRepo.findActiveDebtsByUserId(userId)
                .stream().map(adminService::toDebtDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, debts));
    }

    /**
     * GET /admin/maintenance/debts/aging/threshold/{days}
     * All debts older than {days} days — e.g. /debts/aging/threshold/60
     * Returns the full debt record including user and currency details.
     */
    @Operation(summary = "Debts older than N days")
    @GetMapping("/debts/aging/threshold/{days}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<UserDebtDto>>> debtsOlderThan(
            @PathVariable int days,
            @PageableDefault(size = 20) Pageable pageable) {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(days);
        Page<UserDebt> page = debtRepo.findDebtsOlderThan(cutoff, pageable);
        return ResponseEntity.ok(ApiResponse.success(null, page.map(adminService::toDebtDto)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // USER MAINTENANCE PROFILE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/users/{userId}/profile
     *
     * Everything about a user's maintenance fee relationship in one call:
     *   - User identity (name, email)
     *   - Whether they currently have active debt, and how much
     *   - Debt age bracket (0–30 / 31–60 / 61–90 / 90+ days)
     *   - Total fees paid lifetime
     *   - Full fee transaction history across all currencies and months
     *   - Monthly wallet activity history (what triggered fees)
     */
    @Operation(summary = "Full maintenance fee profile for a user")
    @GetMapping("/users/{userId}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<UserMaintenanceProfileDto>> userProfile(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, adminService.getUserProfile(userId)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REPORTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/reports/month/{monthYear}
     *
     * Billing summary for a specific month:
     *   - Status breakdown (how many DEDUCTED / DEBT / PARTIAL / etc.)
     *   - Total amount collected
     *   - Total outstanding debt created that month
     */
    @Operation(summary = "Monthly billing summary report")
    @GetMapping("/reports/month/{monthYear}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<MonthlyStatusReportDto>> monthlyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthYear) {
        return ResponseEntity.ok(ApiResponse.success(null,
                adminService.getMonthlyStatusReport(monthYear)));
    }

    /**
     * GET /admin/maintenance/reports/months
     * List of all billing months that have records.
     * Use this to populate the month picker in the UI.
     */
    @Operation(summary = "All available billing months")
    @GetMapping("/reports/months")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<List<LocalDate>>> availableMonths() {
        return ResponseEntity.ok(ApiResponse.success(null,
                feeTransactionRepo.findDistinctMonths()));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MONTHLY ACTIVITY (USAGE TRACKING)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/activity/user/{userId}
     * All monthly activity records for a user (which months used which currencies).
     */
    @Operation(summary = "Monthly wallet activity history for a user")
    @GetMapping("/activity/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<List<UserMonthlyActivityDto>>> userActivity(
            @PathVariable Long userId) {
        List<UserMonthlyActivityDto> activity = activityRepo.findByUserId(userId)
                .stream().map(adminService::toActivityDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, activity));
    }

    /**
     * GET /admin/maintenance/activity/user/{userId}/month/{monthYear}
     * Activity for a specific user in a specific month (per-currency breakdown).
     */
    @Operation(summary = "User activity for a specific billing month")
    @GetMapping("/activity/user/{userId}/month/{monthYear}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<List<UserMonthlyActivityDto>>> userActivityForMonth(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthYear) {
        List<UserMonthlyActivityDto> activity =
                activityRepo.findByUserIdAndMonth(userId, monthYear.withDayOfMonth(1))
                        .stream().map(adminService::toActivityDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, activity));
    }

    /**
     * GET /admin/maintenance/activity/month/{monthYear}/pending-count
     * How many (userId, currency) entries are unprocessed for this month.
     * Use before triggering a manual batch to confirm there's work to do.
     */
    @Operation(summary = "Count of unprocessed activity entries for a billing month")
    @GetMapping("/activity/month/{monthYear}/pending-count")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Long>> pendingCount(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthYear) {
        long count = activityRepo.countUnprocessedForMonth(monthYear.withDayOfMonth(1));
        return ResponseEntity.ok(ApiResponse.success(null, count));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AUDIT LOGS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/maintenance/audit
     * GET /admin/maintenance/audit?batchId=abc-uuid
     * GET /admin/maintenance/audit?userId=42
     *
     * Full audit trail of every maintenance fee action.
     * Filter by batchId to see everything from one scheduler run.
     * Filter by userId to see a user's complete audit history.
     * Leave both empty to get all records paginated.
     */
    @Operation(summary = "Maintenance fee audit log")
    @GetMapping("/audit")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> auditLogs(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                adminService.getAuditLogs(batchId, userId, pageable)));
    }

    /**
     * GET /admin/maintenance/audit/batch/{batchId}/stats
     * Count of each action type within a batch run.
     * e.g. { "DEDUCTED": 1200, "DEBT_CREATED": 84, "FAILED": 3 }
     */
    @Operation(summary = "Action summary stats for a batch run")
    @GetMapping("/audit/batch/{batchId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> batchStats(
            @PathVariable String batchId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                adminService.getBatchStats(batchId)));
    }

    /**
     * GET /admin/maintenance/audit/batch/{batchId}/failures
     * All failed entries in a specific batch run — for investigation and retry.
     */
    @Operation(summary = "Failed actions in a batch run")
    @GetMapping("/audit/batch/{batchId}/failures")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> batchFailures(
            @PathVariable String batchId) {
        List<AuditLogDto> failures = auditLogRepo.findFailuresByBatchId(batchId)
                .stream().map(l -> AuditLogDto.builder()
                        .id(l.getId())
                        .batchId(l.getBatchId())
                        .userId(l.getUserId())
                        .action(l.getAction() != null ? l.getAction().name() : null)
                        .status(l.getStatus() != null ? l.getStatus().name() : null)
                        .details(l.getDetails())
                        .errorMessage(l.getErrorMessage())
                        .createdAt(l.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(null, failures));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BATCH CONTROL (SUPER_USER ONLY)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * POST /admin/maintenance/batch/trigger
     * POST /admin/maintenance/batch/trigger?billingMonth=2026-08-01
     *
     * Manually triggers a full maintenance fee batch for the given month.
     * Acquires a distributed Redis lock — returns 409 if another run is in progress.
     * Only SUPER_USER can do this — it writes to every active user's wallet.
     *
     * billingMonth must be the 1st of the target month.
     * Omit to default to last calendar month.
     */
    @Operation(summary = "Trigger a maintenance fee batch run (SUPER_USER only)")
    @PostMapping("/batch/trigger")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<ApiResponse<MaintenanceBatchSummaryDto>> triggerBatch(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth) {
        LocalDate month = (billingMonth != null)
                ? billingMonth.withDayOfMonth(1)
                : LocalDate.now().minusMonths(1).withDayOfMonth(1);
        MaintenanceBatchSummaryDto summary = scheduler.runBatchForMonth(month);
        if (summary == null) {
            return ResponseEntity.status(409).body(
                    ApiResponse.success("A batch is already running for this month. Try again later.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Batch completed successfully", summary));
    }
}
