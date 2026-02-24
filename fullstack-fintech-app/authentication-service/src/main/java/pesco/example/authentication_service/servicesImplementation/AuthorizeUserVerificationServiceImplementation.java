package pesco.example.authentication_service.servicesImplementation;

import org.springframework.stereotype.Service;
import pesco.example.authentication_service.models.AuthorizeUserVerification;
import pesco.example.authentication_service.repositories.AuthorizeUserVerificationRepository;
import pesco.example.authentication_service.services.AuthorizeUserVerificationService;


@Service
public class AuthorizeUserVerificationServiceImplementation implements AuthorizeUserVerificationService {

    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;

    public AuthorizeUserVerificationServiceImplementation(AuthorizeUserVerificationRepository authorizeUserVerificationRepository) {
        this.authorizeUserVerificationRepository = authorizeUserVerificationRepository;
    }

    @Override
    public void save(Long userId, Long id) {
        AuthorizeUserVerification auth = new AuthorizeUserVerification();
        auth.setId(id);
        auth.setUserId(userId);
        authorizeUserVerificationRepository.save(auth);
    }

}
