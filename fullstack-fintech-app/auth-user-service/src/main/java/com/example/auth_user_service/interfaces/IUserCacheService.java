package com.example.auth_user_service.interfaces;

import java.util.List;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.payloads.CacheStats;

public interface IUserCacheService {
    void startCacheWarmup();

    CacheStats getCacheStats();

    void cacheUserById(Long userId);

    void cleanup();

    void refreshCache();

    boolean isWarmupInProgress();

    void invalidateAllCache();

    void invalidateUserCache(Long userId);

    UserDTO getCachedUserById(Long userId);

    List<UserDTO> getCachedAllUsers();

    void initializeCache();
}
