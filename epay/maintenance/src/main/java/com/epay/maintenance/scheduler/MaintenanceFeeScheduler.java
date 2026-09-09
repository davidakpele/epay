package com.epay.maintenance.scheduler;

import com.epay.domain.maintenance.dto.MaintenanceBatchSummaryDto;
import com.epay.domain.maintenance.entity.UserMonthlyActivity;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import com.epay.maintenance.repository.UserMonthlyActivityRepository;
import com.epay.maintenance.service.MaintenanceFeeEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceFeeScheduler {

    private static final String LOCK_KEY    = "epay:lock:maintenance-batch";
    private static final Duration LOCK_TTL  = Duration.ofHours(2);

    private final UserMonthlyActivityRepository activityRepository;
    private final MaintenanceFeeEngine          feeEngine;
    private final StringRedisTemplate           redisTemplate;

    @Value("${spring.instance.id:${INSTANCE_ID:unknown}}")
    private String instanceId;

    @Value("${epay.maintenance.batch-size:500}")
    private int batchSize;

    @Scheduled(cron = "${epay.maintenance.cron:0 30 0 1 * *}")
    public void runMonthlyBatch() {
        LocalDate billingMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        runBatchForMonth(billingMonth);
    }

    public MaintenanceBatchSummaryDto runBatchForMonth(LocalDate billingMonth) {
        if (!acquireLock(billingMonth)) {
            log.warn("[MaintenanceScheduler] Lock held by another instance — skipping {}", billingMonth);
            return null;
        }

        String batchId      = UUID.randomUUID().toString();
        LocalDateTime start = LocalDateTime.now();

        log.info("[MaintenanceScheduler] === Batch START === instance={} batchId={} billingMonth={}",
                instanceId, batchId, billingMonth);

        int totalProcessed        = 0;
        int totalDeducted         = 0;
        int totalPartial          = 0;
        int totalDebt             = 0;
        int totalFailed           = 0;
        int totalSkipped          = 0;
        BigDecimal totalDeducted$ = BigDecimal.ZERO;
        BigDecimal totalDebt$     = BigDecimal.ZERO;

        try {
            long totalCount = activityRepository.countUnprocessedForMonth(billingMonth);
            log.info("[MaintenanceScheduler] {} unprocessed entries for {}", totalCount, billingMonth);

            int page = 0;
            Page<UserMonthlyActivity> chunk;

            do {
                chunk = activityRepository.findUnprocessedForMonth(
                        billingMonth, PageRequest.of(page, batchSize));

                for (UserMonthlyActivity activity : chunk.getContent()) {
                    try {
                        MaintenanceFeeStatus outcome = feeEngine.charge(
                                activity.getUserId(),
                                activity.getCurrencyCode(),
                                billingMonth,
                                batchId);
                        activityRepository.markProcessed(activity.getId());

                        switch (outcome) {
                            case DEDUCTED -> totalDeducted++;
                            case PARTIAL  -> totalPartial++;
                            case DEBT     -> totalDebt++;
                            case FAILED   -> totalFailed++;
                            default       -> totalSkipped++;
                        }
                        totalProcessed++;

                    } catch (Exception ex) {
                        totalFailed++;
                        totalProcessed++;
                        log.error("[MaintenanceScheduler] FAILED userId={} currency={}: {}",
                                activity.getUserId(), activity.getCurrencyCode(), ex.getMessage(), ex);
                    }
                }

                log.info("[MaintenanceScheduler] Page {}/{} processed. Running total: {}",
                        page + 1, chunk.getTotalPages(), totalProcessed);
                page++;

            } while (chunk.hasNext());

        } finally {
            releaseLock();
        }

        LocalDateTime end = LocalDateTime.now();

        MaintenanceBatchSummaryDto summary = MaintenanceBatchSummaryDto.builder()
                .batchId(batchId)
                .billingMonth(billingMonth)
                .startedAt(start)
                .completedAt(end)
                .totalUsersProcessed(totalProcessed)
                .totalCurrenciesCharged(totalDeducted + totalPartial)
                .successfulDeductions(totalDeducted)
                .partialDeductions(totalPartial)
                .newDebtsCreated(totalDebt)
                .failedCharges(totalFailed)
                .skippedAlreadyProcessed(totalSkipped)
                .totalAmountDeducted(totalDeducted$)
                .totalDebtCreated(totalDebt$)
                .build();

        log.info("[MaintenanceScheduler] === Batch COMPLETE === {}", summary);
        return summary;
    }

    private boolean acquireLock(LocalDate billingMonth) {

        String lockKey = LOCK_KEY + ":" + billingMonth;
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, instanceId, LOCK_TTL);
            if (Boolean.TRUE.equals(acquired)) {
                log.info("[MaintenanceScheduler] Lock acquired: key={} instance={}", lockKey, instanceId);
                return true;
            }
            String holder = redisTemplate.opsForValue().get(lockKey);
            log.warn("[MaintenanceScheduler] Lock already held by instance={}", holder);
            return false;
        } catch (Exception ex) {
            log.error("[MaintenanceScheduler] Redis lock check failed — proceeding without lock: {}",
                    ex.getMessage());
            return true;
        }
    }

    private void releaseLock() {
        try {
            String lockKey = LOCK_KEY + ":" + LocalDate.now().minusMonths(1).withDayOfMonth(1);
            String holder  = redisTemplate.opsForValue().get(lockKey);
            if (instanceId.equals(holder)) {
                redisTemplate.delete(lockKey);
                log.info("[MaintenanceScheduler] Lock released: key={}", lockKey);
            }
        } catch (Exception ex) {
            log.warn("[MaintenanceScheduler] Lock release failed (will expire via TTL): {}", ex.getMessage());
        }
    }
}
