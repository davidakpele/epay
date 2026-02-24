package pesco.example.authentication_service.servicesImplementation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import pesco.example.authentication_service.configurations.CacheConfig;
import pesco.example.authentication_service.dtos.UserDTO;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.repositories.UsersRepository;

@Service
public class UserCacheService {

    private static final String ALL_USERS_KEY = "users:all";
    private static final String USER_BY_ID_KEY = "user:%d";
    private static final String LAST_CACHED_ID_KEY = "cache:last_cached_user_id";
    private static final String CACHE_INITIALIZED_KEY = "cache:initialized";

    private final AtomicBoolean warmupInProgress = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final RedisTemplate<String, Object> redisTemplate;
    private final UsersRepository userRepository;
    private final ObjectMapper objectMapper;
    private final CacheConfig cacheConfig;

    public UserCacheService(RedisTemplate<String,Object> redisTemplate,
                            UsersRepository userRepository,
                            ObjectMapper objectMapper,
                            CacheConfig cacheConfig) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.cacheConfig = cacheConfig;
    }

    @Scheduled(fixedRateString = "${app.cache.warmup-interval:600000}")
    public void startCacheWarmup() {
        if (!warmupInProgress.compareAndSet(false, true)) return;

        try {
            CompletableFuture<Void> incrementalCacheFuture = CompletableFuture.runAsync(this::cacheUsersIncrementally, executorService);
            CompletableFuture<Void> activeUsersFuture = CompletableFuture.runAsync(this::cacheRecentActiveUsers, executorService);

            CompletableFuture.allOf(incrementalCacheFuture, activeUsersFuture)
                    .thenRun(() -> warmupInProgress.set(false))
                    .exceptionally(throwable -> {
                        warmupInProgress.set(false);
                        return null;
                    });
        } catch (Exception e) {
            warmupInProgress.set(false);
        }
    }

    private void cacheUsersIncrementally() {
        try {
            Boolean cacheInitialized = redisTemplate.hasKey(CACHE_INITIALIZED_KEY);

            if (cacheInitialized == null || !cacheInitialized) {
                performFullCacheLoad();
                return;
            }

            Long lastCachedUserId = getLastCachedUserId();
            if (lastCachedUserId == null || lastCachedUserId == 0L) {
                performFullCacheLoad();
                return;
            }

            int pageSize = cacheConfig.getPageSize();
            List<UserDTO> newUsers = new ArrayList<>();
            Pageable pageable = PageRequest.ofSize(pageSize);

            boolean hasMoreData = true;
            Long maxUserId = lastCachedUserId;
            int pageCounter = 0;

            while (hasMoreData) {
                List<Users> usersPage = userRepository.findUsersAfterId(lastCachedUserId, pageable);

                if (usersPage.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                List<UserDTO> userDTOs = usersPage.stream()
                        .map(UserDTO::fromEntity)
                        .collect(Collectors.toList());
                newUsers.addAll(userDTOs);

                Long currentMaxId = usersPage.stream()
                        .map(Users::getId)
                        .max(Long::compareTo)
                        .orElse(lastCachedUserId);

                lastCachedUserId = currentMaxId;
                maxUserId = Math.max(maxUserId, currentMaxId);

                pageCounter++;
                if (pageCounter > 100) break;

                if (usersPage.size() < pageSize) hasMoreData = false;
            }

            if (!newUsers.isEmpty()) {
                mergeNewUsersWithCache(newUsers);
                updateLastCachedUserId(maxUserId);
            }
        } catch (Exception e) {
            redisTemplate.delete(CACHE_INITIALIZED_KEY);
            throw new RuntimeException("Incremental caching failed", e);
        }
    }

    private void performFullCacheLoad() {
        try {
            int pageSize = cacheConfig.getPageSize();
            List<UserDTO> allUsers = new ArrayList<>();
            int page = 0;
            boolean hasMoreData = true;
            Long maxUserId = 0L;

            while (hasMoreData) {
                Pageable pageable = PageRequest.of(page, pageSize);
                List<Users> usersPage = userRepository.findAllUsersPaginated(pageable);

                if (usersPage.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                List<UserDTO> userDTOs = usersPage.stream()
                        .map(UserDTO::fromEntity)
                        .collect(Collectors.toList());
                allUsers.addAll(userDTOs);

                Long currentMaxId = usersPage.stream()
                        .map(Users::getId)
                        .max(Long::compareTo)
                        .orElse(0L);
                maxUserId = Math.max(maxUserId, currentMaxId);

                page++;
                if (page > 100) break;
                if (usersPage.size() < pageSize) hasMoreData = false;
            }

            if (!allUsers.isEmpty()) {
                saveUsersToCache(allUsers);
                updateLastCachedUserId(maxUserId);
                redisTemplate.opsForValue().set(CACHE_INITIALIZED_KEY, "true");
            }
        } catch (Exception e) {
            throw new RuntimeException("Full cache load failed", e);
        }
    }

    private void saveUsersToCache(List<UserDTO> users) {
        try {
            String jsonData = objectMapper.writeValueAsString(users);
            redisTemplate.opsForValue().set(ALL_USERS_KEY, jsonData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cache save failed", e);
        }
    }

    private Long getLastCachedUserId() {
        try {
            String lastIdStr = (String) redisTemplate.opsForValue().get(LAST_CACHED_ID_KEY);
            return lastIdStr != null ? Long.parseLong(lastIdStr) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void updateLastCachedUserId(Long lastId) {
        if (lastId != null && lastId > 0) {
            redisTemplate.opsForValue().set(LAST_CACHED_ID_KEY, lastId.toString());
        }
    }

    private void mergeNewUsersWithCache(List<UserDTO> newUsers) {
        List<UserDTO> existingUsers = getCachedAllUsers();
        if (existingUsers == null) existingUsers = new ArrayList<>();

        Map<Long, UserDTO> userMap = new HashMap<>();
        for (UserDTO existingUser : existingUsers) userMap.put(existingUser.getId(), existingUser);

        for (UserDTO newUser : newUsers) userMap.put(newUser.getId(), newUser);

        List<UserDTO> mergedUsers = new ArrayList<>(userMap.values());
        saveUsersToCache(mergedUsers);
    }

    private int getCachedUsersCount() {
        List<UserDTO> cachedUsers = getCachedAllUsers();
        return cachedUsers != null ? cachedUsers.size() : 0;
    }

    public CacheStats getCacheStats() {
        try {
            Boolean allUsersExists = redisTemplate.hasKey(ALL_USERS_KEY);
            var userKeys = redisTemplate.keys("user:*");
            int userKeysCount = userKeys != null ? userKeys.size() : 0;
            int cachedUsersCount = getCachedUsersCount();

            return new CacheStats(
                allUsersExists != null && allUsersExists,
                cachedUsersCount,
                userKeysCount,
                warmupInProgress.get()
            );
        } catch (Exception e) {
            return new CacheStats(false, 0, 0, warmupInProgress.get());
        }
    }

    private void cacheRecentActiveUsers() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Long> recentUserIds;

        try {
            recentUserIds = userRepository.findRecentUserIds(oneWeekAgo);
        } catch (Exception e) {
            recentUserIds = new ArrayList<>();
        }

        if (recentUserIds.isEmpty()) return;

        List<CompletableFuture<Void>> futures = recentUserIds.stream()
                .map(userId -> CompletableFuture.runAsync(() -> cacheUserById(userId), executorService))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(2, TimeUnit.MINUTES);
        } catch (Exception ignored) {}
    }

    public void cacheUserById(Long userId) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            UserDTO userDTO = UserDTO.fromEntity(user);
            String jsonData = objectMapper.writeValueAsString(userDTO);
            String key = String.format(USER_BY_ID_KEY, userId);
            redisTemplate.opsForValue().set(key, jsonData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to cache user: " + userId, e);
        }
    }

    public List<UserDTO> getCachedAllUsers() {
        try {
            String jsonData = (String) redisTemplate.opsForValue().get(ALL_USERS_KEY);
            if (jsonData == null) return null;
            return objectMapper.readValue(jsonData, objectMapper.getTypeFactory().constructCollectionType(List.class, UserDTO.class));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public UserDTO getCachedUserById(Long userId) {
        try {
            String key = String.format(USER_BY_ID_KEY, userId);
            String jsonData = (String) redisTemplate.opsForValue().get(key);
            if (jsonData == null) return null;
            return objectMapper.readValue(jsonData, UserDTO.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void invalidateUserCache(Long userId) {
        String key = String.format(USER_BY_ID_KEY, userId);
        redisTemplate.delete(key);
    }

    public void invalidateAllCache() {
        redisTemplate.delete(ALL_USERS_KEY);
        redisTemplate.delete(LAST_CACHED_ID_KEY);
        redisTemplate.delete(CACHE_INITIALIZED_KEY);
        var keys = redisTemplate.keys("user:*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }

    public void refreshCache() {
        startCacheWarmup();
    }

    public boolean isWarmupInProgress() {
        return warmupInProgress.get();
    }

    @PostConstruct
    public void initializeCache() {
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
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) executorService.shutdownNow();
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static class CacheStats {
        private boolean allUsersCached;
        private int cachedUsersCount;
        private int individualUserKeysCount;
        private boolean warmupInProgress;

        public CacheStats() {}
        public CacheStats(boolean allUsersCached, int cachedUsersCount, int individualUserKeysCount, boolean warmupInProgress) {
            this.allUsersCached = allUsersCached;
            this.cachedUsersCount = cachedUsersCount;
            this.individualUserKeysCount = individualUserKeysCount;
            this.warmupInProgress = warmupInProgress;
        }

        public boolean isAllUsersCached() { return allUsersCached; }
        public int getCachedUsersCount() { return cachedUsersCount; }
        public int getIndividualUserKeysCount() { return individualUserKeysCount; }
        public boolean isWarmupInProgress() { return warmupInProgress; }

        public void setAllUsersCached(boolean allUsersCached) { this.allUsersCached = allUsersCached; }
        public void setCachedUsersCount(int cachedUsersCount) { this.cachedUsersCount = cachedUsersCount; }
        public void setIndividualUserKeysCount(int individualUserKeysCount) { this.individualUserKeysCount = individualUserKeysCount; }
        public void setWarmupInProgress(boolean warmupInProgress) { this.warmupInProgress = warmupInProgress; }
    }
}
