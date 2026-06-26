package com.pesco.wallet_service.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesco.wallet_service.configuration.CacheConfig;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.repository.WalletRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WalletCacheService {

    private static final Logger log = LoggerFactory.getLogger(WalletCacheService.class);

    private static final String ALL_WALLETS_KEY = "wallet:all";
    private static final String WALLET_BY_ID_KEY = "wallet:%d";
    private static final String LAST_CACHED_ID_KEY = "wallet:cache:last_id";
    private static final String CACHE_INITIALIZED_KEY = "wallet:cache:initialized";
    private static final String CACHE_LOCK_KEY = "wallet:cache:lock";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;
    private final CacheConfig cacheConfig;
    private final ExecutorService executorService;
    private final AtomicBoolean warmupInProgress = new AtomicBoolean(false);

    public WalletCacheService(
            RedisTemplate<String, Object> redisTemplate,
            WalletRepository walletRepository,
            ObjectMapper objectMapper,
            CacheConfig cacheConfig
    ) {
        this.redisTemplate = redisTemplate;
        this.walletRepository = walletRepository;
        this.objectMapper = objectMapper;
        this.cacheConfig = cacheConfig;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /** Scheduled warmup — runs periodically (default: every 10 minutes) */
    @Scheduled(fixedRateString = "${app.cache.warmup-interval:600000}")
    public void startCacheWarmup() {
        if (!warmupInProgress.compareAndSet(false, true)) {
            log.warn("Wallet cache warmup already running — skipping...");
            return;
        }

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(CACHE_LOCK_KEY, "locked", Duration.ofMinutes(15));
        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("Another instance is performing wallet cache warmup. Skipping...");
            warmupInProgress.set(false);
            return;
        }

        log.info("Starting wallet cache warmup...");
        try {
            CompletableFuture<Void> incrementalFuture = CompletableFuture.runAsync(this::cacheWalletsIncrementally, executorService);
            CompletableFuture<Void> recentFuture = CompletableFuture.runAsync(this::cacheRecentActiveWallets, executorService);

            CompletableFuture.allOf(incrementalFuture, recentFuture)
                    .thenRun(() -> {
                        log.info("Wallet cache warmup completed successfully.");
                        warmupInProgress.set(false);
                        redisTemplate.delete(CACHE_LOCK_KEY);
                    })
                    .exceptionally(e -> {
                        log.error("Wallet cache warmup failed: {}", e.getMessage(), e);
                        warmupInProgress.set(false);
                        redisTemplate.delete(CACHE_LOCK_KEY);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error starting wallet cache warmup", e);
            warmupInProgress.set(false);
            redisTemplate.delete(CACHE_LOCK_KEY);
        }
    }

    /** Incremental cache update — caches new wallets after last cached ID */
    private void cacheWalletsIncrementally() {
        log.info("Running incremental wallet caching...");
        long start = System.currentTimeMillis();

        try {
            Boolean initialized = redisTemplate.hasKey(CACHE_INITIALIZED_KEY);
            if (initialized == null || !initialized) {
                log.info("Wallet cache not initialized — performing full cache load.");
                performFullCacheLoad();
                return;
            }

            Long lastCachedWalletId = getLastCachedWalletId();
            if (lastCachedWalletId == null || lastCachedWalletId == 0L) {
                performFullCacheLoad();
                return;
            }

            int pageSize = cacheConfig.getPageSize();
            int page = 0;
            boolean hasMore = true;
            long maxWalletId = lastCachedWalletId;

            while (hasMore) {
                List<Wallet> walletsPage = walletRepository.findWalletsAfterId(lastCachedWalletId, page, pageSize);
                if (walletsPage.isEmpty()) break;

                saveWalletsToCache(walletsPage);

                long currentMaxId = walletsPage.stream()
                        .map(Wallet::getId)
                        .max(Long::compareTo)
                        .orElse(lastCachedWalletId);
                maxWalletId = Math.max(maxWalletId, currentMaxId);

                hasMore = walletsPage.size() == pageSize;
                page++;
            }

            updateLastCachedWalletId(maxWalletId);
            log.info("Incremental wallet cache update completed in {} ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Error during incremental wallet caching", e);
            redisTemplate.delete(CACHE_INITIALIZED_KEY);
        }
    }

    /** Full cache load — loads all wallets into Redis */
    private void performFullCacheLoad() {
        log.info("Performing full wallet cache load...");
        long start = System.currentTimeMillis();

        try {
            int pageSize = cacheConfig.getPageSize();
            int page = 0;
            boolean hasMore = true;
            long maxWalletId = 0L;
            int total = 0;

            while (hasMore) {
                List<Wallet> walletsPage = walletRepository.findAllWalletsPaginated(page, pageSize);
                if (walletsPage.isEmpty()) break;

                saveWalletsToCache(walletsPage);
                total += walletsPage.size();

                long currentMaxId = walletsPage.stream()
                        .map(Wallet::getId)
                        .max(Long::compareTo)
                        .orElse(0L);
                maxWalletId = Math.max(maxWalletId, currentMaxId);

                hasMore = walletsPage.size() == pageSize;
                page++;
            }

            updateLastCachedWalletId(maxWalletId);
            redisTemplate.opsForValue().set(CACHE_INITIALIZED_KEY, "true");
            log.info("Full wallet cache load completed — {} entries cached in {} ms", total, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Full wallet cache load failed", e);
        }
    }

    /** Save wallets to cache individually by userId */
    private void saveWalletsToCache(List<Wallet> wallets) {
        wallets.forEach(wallet -> {
            try {
                String key = String.format(WALLET_BY_ID_KEY, wallet.getUserId());
                String json = objectMapper.writeValueAsString(wallet);
                redisTemplate.opsForValue().set(key, json, Duration.ofHours(1));
            } catch (JsonProcessingException e) {
                log.error("Failed to cache wallet for user {}", wallet.getUserId(), e);
            }
        });
    }

    private Long getLastCachedWalletId() {
        try {
            String idStr = (String) redisTemplate.opsForValue().get(LAST_CACHED_ID_KEY);
            return idStr != null ? Long.parseLong(idStr) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void updateLastCachedWalletId(Long id) {
        redisTemplate.opsForValue().set(LAST_CACHED_ID_KEY, id.toString());
    }

    /** Cache recent active wallets (updated in the last 7 days) */
    public void cacheRecentActiveWallets() {
        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            List<Long> recentWalletIds = walletRepository.findRecentWalletIds(oneWeekAgo);
            if (recentWalletIds.isEmpty()) return;

            AtomicInteger success = new AtomicInteger();
            recentWalletIds.parallelStream().forEach(id -> {
                try {
                    cacheWalletById(id);
                    success.incrementAndGet();
                } catch (Exception e) {
                    log.warn("Failed caching wallet {}", id, e);
                }
            });

            log.info("Cached {} recent wallets successfully.", success.get());
        } catch (Exception e) {
            log.error("Error caching recent wallets", e);
        }
    }

    /** Cache a specific wallet by ID */
    public void cacheWalletById(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        try {
            String key = String.format(WALLET_BY_ID_KEY, wallet.getUserId());
            String json = objectMapper.writeValueAsString(wallet);
            redisTemplate.opsForValue().set(key, json, Duration.ofHours(1));
        } catch (JsonProcessingException e) {
            log.error("Failed to cache wallet {}", walletId, e);
        }
    }

    /** Retrieve cached wallet by userId */
    public Wallet getCachedWalletByUserId(Long userId) {
        try {
            String key = String.format(WALLET_BY_ID_KEY, userId);
            Object jsonObj = redisTemplate.opsForValue().get(key);
            if (jsonObj == null) return null;
            return objectMapper.readValue(String.valueOf(jsonObj), Wallet.class);
        } catch (JsonProcessingException e) {
            log.error("Error reading wallet cache for user {}", userId, e);
            return null;
        }
    }

    /** Invalidate all wallet cache entries */
    public void invalidateAllCache() {
        redisTemplate.delete(ALL_WALLETS_KEY);
        redisTemplate.delete(LAST_CACHED_ID_KEY);
        redisTemplate.delete(CACHE_INITIALIZED_KEY);
        log.info("Invalidated all wallet caches.");
    }

    @PostConstruct
    public void initializeCache() {
        log.info("Initializing WalletCacheService... scheduling initial warmup in 30s");
        executorService.submit(() -> {
            try {
                Thread.sleep(30000);
                startCacheWarmup();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down WalletCacheService executor...");
        executorService.shutdown();
    }
}
