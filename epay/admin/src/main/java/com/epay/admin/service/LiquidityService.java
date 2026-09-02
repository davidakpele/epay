package com.epay.admin.service;

import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.liquidity.dto.LiquidityStatusDTO;
import com.epay.domain.liquidity.entity.LiquidityConfig;
import com.epay.domain.liquidity.entity.PlatformLedger;
import com.epay.domain.liquidity.input.UpdateLiquidityConfigRequest;
import com.epay.domain.liquidity.repository.LiquidityConfigRepository;
import com.epay.domain.liquidity.repository.PlatformLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiquidityService {

    private static final String GATEWAY_PAYSTACK    = "PAYSTACK";
    private static final String GATEWAY_FLUTTERWAVE = "FLUTTERWAVE";

    @Value("${epay.gateways.paystack.secret-key:}")
    private String paystackSecretKey;

    @Value("${flutterwave.secret-key:}")
    private String flutterwaveSecretKey;

    private final LiquidityConfigRepository configRepository;
    private final PlatformLedgerRepository  ledgerRepository;
    private final RestTemplate              restTemplate;

    public LiquidityStatusDTO getPaystackStatus() {
        return buildStatus(GATEWAY_PAYSTACK);
    }

    public LiquidityStatusDTO getFlutterwaveStatus() {
        return buildStatus(GATEWAY_FLUTTERWAVE);
    }

    @Transactional
    public LiquidityStatusDTO syncPaystackBalance(String triggeredBy) {
        BigDecimal balance = fetchPaystackBalance();
        return persistSync(GATEWAY_PAYSTACK, balance, "NGN", triggeredBy);
    }

    @Transactional
    public LiquidityStatusDTO syncFlutterwaveBalance(String triggeredBy) {
        BigDecimal balance = fetchFlutterwaveBalance();
        return persistSync(GATEWAY_FLUTTERWAVE, balance, "NGN", triggeredBy);
    }

    @Transactional
    public PlatformLedger recordManualTopUp(String gateway, BigDecimal amount,
                                             String currency, String description,
                                             String recordedBy) {
        LiquidityConfig cfg = requireConfig(gateway);
        BigDecimal newBalance = (cfg.getLastKnownBalance() != null
                ? cfg.getLastKnownBalance() : BigDecimal.ZERO).add(amount);

        cfg.setLastKnownBalance(newBalance);
        cfg.setLastSyncAt(LocalDateTime.now());
        configRepository.save(cfg);

        PlatformLedger entry = PlatformLedger.builder()
                .gateway(gateway.toUpperCase())
                .entryType("MANUAL_TOPUP")
                .amount(amount)
                .balanceAfter(newBalance)
                .currency(currency)
                .reference("TOPUP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .description(description)
                .recordedBy(recordedBy)
                .build();
        ledgerRepository.save(entry);
        log.info("[Liquidity] Manual top-up recorded: gateway={} amount={} by={}", gateway, amount, recordedBy);
        return entry;
    }

    @Transactional
    public void recordPayoutDeduction(String gateway, BigDecimal amount, String currency,
                                       String reference, Long userId) {
        if (ledgerRepository.existsByReference(reference)) {
            log.debug("[Liquidity] Payout deduction already recorded ref={}", reference);
            return;
        }
        configRepository.findByGateway(gateway.toUpperCase()).ifPresent(cfg -> {
            BigDecimal newBalance = (cfg.getLastKnownBalance() != null
                    ? cfg.getLastKnownBalance() : BigDecimal.ZERO).subtract(amount);
            cfg.setLastKnownBalance(newBalance);
            configRepository.save(cfg);

            ledgerRepository.save(PlatformLedger.builder()
                    .gateway(gateway.toUpperCase())
                    .entryType("PAYOUT_DEDUCTED")
                    .amount(amount.negate())
                    .balanceAfter(newBalance)
                    .currency(currency)
                    .reference(reference)
                    .relatedUserId(userId)
                    .description("Payout deducted for userId=" + userId)
                    .build());

            checkAndAlert(cfg, newBalance);
        });
    }

    public boolean canProcessPayout(String gateway, BigDecimal payoutAmount) {
        return configRepository.findByGateway(gateway.toUpperCase()).map(cfg -> {
            BigDecimal available = cfg.getLastKnownBalance() != null
                    ? cfg.getLastKnownBalance() : BigDecimal.valueOf(Long.MAX_VALUE);
            if (available.compareTo(cfg.getBlockThreshold()) <= 0) {
                log.warn("[Liquidity] PAYOUT BLOCKED — balance={} below blockThreshold={}",
                        available, cfg.getBlockThreshold());
                return false;
            }
            if (available.compareTo(payoutAmount) < 0) {
                log.warn("[Liquidity] PAYOUT BLOCKED — balance={} less than payout={}",
                        available, payoutAmount);
                return false;
            }
            return true;
        }).orElse(true);
    }

    @Transactional
    public LiquidityConfig updateConfig(String gateway, UpdateLiquidityConfigRequest request) {
        LiquidityConfig cfg = configRepository.findByGateway(gateway.toUpperCase())
                .orElse(LiquidityConfig.builder()
                        .id(gateway.equalsIgnoreCase(GATEWAY_PAYSTACK) ? 1L : 2L)
                        .gateway(gateway.toUpperCase())
                        .currency("NGN")
                        .alertsEnabled(true)
                        .alertThreshold(BigDecimal.ZERO)
                        .blockThreshold(BigDecimal.ZERO)
                        .build());

        if (request.getAlertThreshold() != null) cfg.setAlertThreshold(request.getAlertThreshold());
        if (request.getBlockThreshold() != null) cfg.setBlockThreshold(request.getBlockThreshold());
        if (request.getAlertEmails()    != null) cfg.setAlertEmails(request.getAlertEmails());
        if (request.getAlertsEnabled()  != null) cfg.setAlertsEnabled(request.getAlertsEnabled());
        return configRepository.save(cfg);
    }

    public Page<PlatformLedger> getLedger(String gateway, Pageable pageable) {
        if (gateway != null && !gateway.isBlank())
            return ledgerRepository.findByGatewayOrderByCreatedAtDesc(gateway.toUpperCase(), pageable);
        return ledgerRepository.findAll(pageable);
    }

    @Scheduled(fixedDelayString = "${epay.liquidity.sync-interval-ms:1800000}")
    public void scheduledSync() {
        log.debug("[Liquidity] Scheduled balance sync started");
        try {
            syncPaystackBalance("scheduler");
        } catch (Exception e) {
            log.warn("[Liquidity] Scheduled Paystack sync failed: {}", e.getMessage());
        }
    }

    private LiquidityStatusDTO buildStatus(String gateway) {
        LiquidityConfig cfg = configRepository.findByGateway(gateway).orElse(null);
        BigDecimal balance = cfg != null && cfg.getLastKnownBalance() != null
                ? cfg.getLastKnownBalance() : BigDecimal.ZERO;

        BigDecimal alertThreshold = cfg != null ? cfg.getAlertThreshold() : BigDecimal.ZERO;
        BigDecimal blockThreshold = cfg != null ? cfg.getBlockThreshold() : BigDecimal.ZERO;
        boolean belowAlert = balance.compareTo(alertThreshold) <= 0;
        boolean belowBlock = balance.compareTo(blockThreshold) <= 0;

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal payoutsToday   = ledgerRepository.sumByGatewayAndTypeAfter(gateway, "PAYOUT_DEDUCTED", today).abs();
        BigDecimal depositsToday  = ledgerRepository.sumByGatewayAndTypeAfter(gateway, "DEPOSIT_RECEIVED", today);

        return LiquidityStatusDTO.builder()
                .gateway(gateway)
                .currency(cfg != null ? cfg.getCurrency() : "NGN")
                .balance(balance)
                .alertThreshold(alertThreshold)
                .blockThreshold(blockThreshold)
                .belowAlertThreshold(belowAlert)
                .belowBlockThreshold(belowBlock)
                .payoutsBlocked(belowBlock)
                .lastSyncAt(cfg != null ? cfg.getLastSyncAt() : null)
                .totalPayoutsToday(payoutsToday)
                .totalDepositsToday(depositsToday)
                .build();
    }

    private LiquidityStatusDTO persistSync(String gateway, BigDecimal balance,
                                            String currency, String triggeredBy) {
        LiquidityConfig cfg = configRepository.findByGateway(gateway)
                .orElse(LiquidityConfig.builder()
                        .id(gateway.equals(GATEWAY_PAYSTACK) ? 1L : 2L)
                        .gateway(gateway)
                        .currency(currency)
                        .alertThreshold(BigDecimal.valueOf(100_000))
                        .blockThreshold(BigDecimal.valueOf(50_000))
                        .alertsEnabled(true)
                        .build());

        cfg.setLastKnownBalance(balance);
        cfg.setLastSyncAt(LocalDateTime.now());
        configRepository.save(cfg);

        String ref = "SYNC-" + System.currentTimeMillis();
        if (!ledgerRepository.existsByReference(ref)) {
            ledgerRepository.save(PlatformLedger.builder()
                    .gateway(gateway)
                    .entryType("BALANCE_SYNC")
                    .amount(BigDecimal.ZERO)
                    .balanceAfter(balance)
                    .currency(currency)
                    .reference(ref)
                    .description("Balance sync triggered by " + triggeredBy)
                    .recordedBy(triggeredBy)
                    .build());
        }

        checkAndAlert(cfg, balance);
        log.info("[Liquidity] {} balance synced: {} {}", gateway, balance, currency);
        return buildStatus(gateway);
    }

    private void checkAndAlert(LiquidityConfig cfg, BigDecimal balance) {
        if (!cfg.isAlertsEnabled()) return;
        if (balance.compareTo(cfg.getBlockThreshold()) <= 0) {
            log.error("[Liquidity] CRITICAL — {} balance {} is BELOW block threshold {}. Payouts are BLOCKED.",
                    cfg.getGateway(), balance, cfg.getBlockThreshold());
        } else if (balance.compareTo(cfg.getAlertThreshold()) <= 0) {
            log.warn("[Liquidity] WARNING — {} balance {} is below alert threshold {}.",
                    cfg.getGateway(), balance, cfg.getAlertThreshold());
        }
    }

    private BigDecimal fetchPaystackBalance() {
        try {
            if (paystackSecretKey == null || paystackSecretKey.isBlank()) {
                log.warn("[Liquidity] Paystack secret key not configured — skipping live balance fetch");
                return BigDecimal.ZERO;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(paystackSecretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.paystack.co/balance",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object dataObj = response.getBody().get("data");
                if (dataObj instanceof java.util.List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> entry) {
                        Number kobo = (Number) entry.get("balance");
                        if (kobo != null)
                            return BigDecimal.valueOf(kobo.longValue())
                                    .divide(BigDecimal.valueOf(100));
                    }
                }
            }
        } catch (Exception e) {
            log.error("[Liquidity] Paystack balance fetch failed: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal fetchFlutterwaveBalance() {
        try {
            if (flutterwaveSecretKey == null || flutterwaveSecretKey.isBlank()) {
                log.warn("[Liquidity] Flutterwave secret key not configured");
                return BigDecimal.ZERO;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(flutterwaveSecretKey);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.flutterwave.com/v3/balances",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object dataObj = response.getBody().get("data");
                if (dataObj instanceof java.util.List<?> list && !list.isEmpty()) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> entry
                                && "NGN".equalsIgnoreCase(String.valueOf(entry.get("currency")))) {
                            Number available = (Number) entry.get("available_balance");
                            if (available != null) return BigDecimal.valueOf(available.doubleValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[Liquidity] Flutterwave balance fetch failed: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private LiquidityConfig requireConfig(String gateway) {
        return configRepository.findByGateway(gateway.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Liquidity config not found for gateway: " + gateway));
    }
}
