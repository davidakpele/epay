package com.epay.auth.service;

import com.epay.auth.interfaces.IUserTracerService;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserTracer;
import com.epay.domain.auth.enums.AttemptType;
import com.epay.domain.auth.repository.UserTracerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTracerService implements IUserTracerService {

    private static final Duration SESSION_TTL = Duration.ofHours(12);
    private static final String   KEY_ACTIVE  = "session:active:";
    private static final String   KEY_BY_ID   = "session:id:";

    private final UserTracerRepository             tracerRepository;
    private final RedisTemplate<String, Object>    redisTemplate;

    @Override
    public boolean hasActiveSession(Long userId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_ACTIVE + userId));
        } catch (Exception e) {
            log.warn("[Session] Redis check failed for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public UserTracer createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime expires = now.plus(SESSION_TTL);

        UserTracer tracer = UserTracer.builder()
                .user(user)
                .attemptType(AttemptType.LOGIN)
                .success(true)
                .sessionId(sessionId)
                .active(true)
                .createdAt(now)
                .expiresAt(expires)
                .build();

        tracerRepository.save(tracer);

        try {
            redisTemplate.opsForValue().set(KEY_ACTIVE + user.getId(), sessionId, SESSION_TTL);
            redisTemplate.opsForValue().set(KEY_BY_ID  + sessionId,   String.valueOf(user.getId()), SESSION_TTL);
        } catch (Exception e) {
            log.warn("[Session] Redis store failed for userId={}: {}", user.getId(), e.getMessage());
        }

        log.info("[Session] Created session={} for userId={}", sessionId, user.getId());
        return tracer;
    }

    @Override
    public void deleteSession(String sessionId) {
        try {
            Object userIdVal = redisTemplate.opsForValue().get(KEY_BY_ID + sessionId);
            if (userIdVal != null) {
                redisTemplate.delete(KEY_ACTIVE + userIdVal);
                redisTemplate.delete(KEY_BY_ID  + sessionId);
            }
        } catch (Exception e) {
            log.warn("[Session] Redis delete failed for sessionId={}: {}", sessionId, e.getMessage());
        }
        log.info("[Session] Deleted session={}", sessionId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        try {
            Object sessionId = redisTemplate.opsForValue().get(KEY_ACTIVE + userId);
            if (sessionId != null) {
                redisTemplate.delete(KEY_BY_ID  + sessionId);
                redisTemplate.delete(KEY_ACTIVE + userId);
            }
        } catch (Exception e) {
            log.warn("[Session] Redis deleteByUser failed for userId={}: {}", userId, e.getMessage());
        }
        log.info("[Session] Deleted all sessions for userId={}", userId);
    }
    @Override
    public boolean isSessionValid(String sessionId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_BY_ID + sessionId));
        } catch (Exception e) {
            log.warn("[Session] Redis validity check failed for sessionId={}: {}", sessionId, e.getMessage());
            return true;
        }
    }
}
