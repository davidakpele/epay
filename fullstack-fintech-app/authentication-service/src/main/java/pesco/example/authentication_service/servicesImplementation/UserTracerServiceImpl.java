package pesco.example.authentication_service.servicesImplementation;

import pesco.example.authentication_service.services.UserTracerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.authentication_service.models.UserTracer;
import pesco.example.authentication_service.models.Users;
import pesco.example.authentication_service.repositories.UserTracerRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserTracerServiceImpl implements UserTracerService{

    private final UserTracerRepository tracerRepository;
    

    public UserTracerServiceImpl(UserTracerRepository tracerRepository) {
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

    // Logout / delete session
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

    // Validate session
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
