package com.payrix.administrator.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.payrix.administrator.models.User;
import com.payrix.administrator.repositories.UserRepository;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    public final UserRepository userRepository;
    
    public CacheService(RedisTemplate<String,Object> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }
    
    
    public void put(String key, Object value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }
    
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Cacheable(value = "users", key = "#id")
    public User getUserByIdWithCache(Long id) {
        // This will automatically cache the result
        return userRepository.findById(id).orElse(null);
    }
}
