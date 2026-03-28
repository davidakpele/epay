package com.example.auth_user_service.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import com.example.auth_user_service.interfaces.IUserTracerService;
import com.example.auth_user_service.models.UserTracer;
import com.example.auth_user_service.models.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.auth_user_service.repositories.UserTracerRepository;

@Service
public class UserTracerService implements  IUserTracerService{

    private final UserTracerRepository tracerRepository;
    
    public UserTracerService(UserTracerRepository tracerRepository) {
        this.tracerRepository = tracerRepository;
    }

    // Check if user has active session
    @Override
    public boolean hasActiveSession(Long userId) {
        Optional<UserTracer> tracer = tracerRepository.findByUserId(userId);

        if (tracer.isEmpty()) return false;

        if (tracer.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            tracer.get().setActive(false);
            tracerRepository.save(tracer.get());
            return false;
        }

        return tracer.get().isActive();
    }

    // Create session for user
    @Transactional
    @Override
    public UserTracer createSession(Users user) {

        tracerRepository.findByUserId(user.getId()).ifPresent(tr -> {
            tr.setActive(false);
            tracerRepository.save(tr);
        });

        UserTracer tracer;
        tracer = new UserTracer(
                null,
                UUID.randomUUID().toString(),
                user,
                LocalDateTime.now().plusHours(24),
                null,
                true
        );

        return tracerRepository.save(tracer);
    }

    @Transactional
    @Override
    public void deleteSession(String sessionId) {
        tracerRepository.deleteBySessionId(sessionId);
    }

    @Transactional
    @Override
    public void deleteByUserId(Long userId) {
        tracerRepository.deleteByUserId(userId);
    }

    @Override
    public boolean isSessionValid(String sessionId) {
        Optional<UserTracer> tracer = tracerRepository.findBySessionId(sessionId);

        if (tracer.isEmpty()) return false;

        UserTracer session = tracer.get();

        if (!session.isActive()) return false;

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setActive(false);
            tracerRepository.save(session);
            return false;
        }

        return true;
    }


}
