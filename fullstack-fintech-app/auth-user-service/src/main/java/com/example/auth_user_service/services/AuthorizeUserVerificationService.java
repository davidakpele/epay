package com.example.auth_user_service.services;

import org.springframework.stereotype.Service;
import com.example.auth_user_service.interfaces.IAuthorizeUserVerificationService;
import com.example.auth_user_service.models.AuthorizeUserVerification;
import com.example.auth_user_service.repositories.AuthorizeUserVerificationRepository;


@Service
public class AuthorizeUserVerificationService implements IAuthorizeUserVerificationService{

    private final AuthorizeUserVerificationRepository authorizeUserVerificationRepository;

    public AuthorizeUserVerificationService(AuthorizeUserVerificationRepository authorizeUserVerificationRepository) {
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
