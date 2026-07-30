package com.example.auth_user_service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.auth_user_service.dtos.UserDTO;
import com.example.auth_user_service.interfaces.IUserCacheService;
import com.example.auth_user_service.payloads.CacheStats;
import com.example.auth_user_service.responses.CacheResponse;
import java.util.List;

@RestController
@RequestMapping("/cache/users")
public class UserCacheController {
    private static final Logger log = LoggerFactory.getLogger(UserCacheController.class);
    private final IUserCacheService userCacheService;

    public UserCacheController(IUserCacheService userCacheService) {
        this.userCacheService = userCacheService;
    }

   @GetMapping("/all")
    public ResponseEntity<CacheResponse<List<UserDTO>>> getAllCachedUsers() {
        try {
            List<UserDTO> cachedUsers = userCacheService.getCachedAllUsers();
            
            if (cachedUsers != null) {
                CacheResponse<List<UserDTO>> response = new CacheResponse<>(
                    true, 
                    null, 
                    cachedUsers, 
                    true, 
                    cachedUsers.size()
                );
                return ResponseEntity.ok()
                        .header("X-Cache-Source", "redis")
                        .body(response);
            } else {
                CacheResponse<List<UserDTO>> response = new CacheResponse<>(
                    true, 
                    "No cached data available", 
                    List.of(), 
                    false, 
                    0
                );
                return ResponseEntity.ok()
                        .header("X-Cache-Source", "miss")
                        .body(response);
            }
            
        } catch (Exception e) {
            log.error("Failed to get cached users", e);
            CacheResponse<List<UserDTO>> response = new CacheResponse<>(
                false, 
                "Failed to get cached data: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CacheResponse<UserDTO>> getCachedUserById(@PathVariable Long userId) {
        try {
            UserDTO cachedUser = userCacheService.getCachedUserById(userId);
            
            if (cachedUser != null) {
                CacheResponse<UserDTO> response = new CacheResponse<>(
                    true, 
                    null, 
                    cachedUser, 
                    true, 
                    1
                );
                return ResponseEntity.ok()
                        .header("X-Cache-Source", "redis")
                        .body(response);
            } else {
                CacheResponse<UserDTO> response = new CacheResponse<>(
                    true, 
                    "User not found in cache", 
                    null, 
                    false, 
                    0
                );
                return ResponseEntity.ok()
                        .header("X-Cache-Source", "miss")
                        .body(response);
            }
            
        } catch (Exception e) {
            log.error("Failed to get cached user {}", userId, e);
            CacheResponse<UserDTO> response = new CacheResponse<>(
                false, 
                "Failed to get cached user: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<CacheResponse<String>> refreshCache() {
        try {
            userCacheService.refreshCache();
            CacheResponse<String> response = new CacheResponse<>(
                true, 
                "Cache refresh initiated", 
                "Cache refresh initiated", 
                userCacheService.isWarmupInProgress(), 
                1
            );
            return ResponseEntity.accepted().body(response);
                    
        } catch (Exception e) {
            log.error("Failed to refresh cache", e);
            CacheResponse<String> response = new CacheResponse<>(
                false, 
                "Failed to refresh cache: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<CacheResponse<CacheStats>> getCacheStats() {
        try {
            CacheStats stats = userCacheService.getCacheStats();
            CacheResponse<CacheStats> response = new CacheResponse<>(
                true, 
                null, 
                stats, 
                true, 
                1
            );
            return ResponseEntity.ok().body(response);      
        } catch (Exception e) {
            log.error("Failed to get cache stats", e);
            CacheResponse<CacheStats> response = new CacheResponse<>(
                false, 
                "Failed to get cache stats: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<CacheResponse<String>> invalidateUserCache(@PathVariable Long userId) {
        try {
            userCacheService.invalidateUserCache(userId);
            CacheResponse<String> response = new CacheResponse<>(
                true, 
                "User cache invalidated", 
                "User cache invalidated", 
                true, 
                1
            );
            return ResponseEntity.ok().body(response);
                    
        } catch (Exception e) {
            log.error("Failed to invalidate user cache {}", userId, e);
            CacheResponse<String> response = new CacheResponse<>(
                false, 
                "Failed to invalidate cache: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<CacheResponse<String>> invalidateAllCache() {
        try {
            userCacheService.invalidateAllCache();
            CacheResponse<String> response = new CacheResponse<>(
                true, 
                "All cache invalidated", 
                "All cache invalidated", 
                true, 
                1
            );
            return ResponseEntity.ok().body(response);
                    
        } catch (Exception e) {
            log.error("Failed to invalidate all cache", e);
            CacheResponse<String> response = new CacheResponse<>(
                false, 
                "Failed to invalidate all cache: " + e.getMessage(), 
                null, 
                false, 
                0
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}