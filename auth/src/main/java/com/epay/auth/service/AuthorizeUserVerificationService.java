package com.epay.auth.service;

import org.springframework.stereotype.Service;
import com.epay.auth.interfaces.IAuthorizeUserVerificationService;
import com.epay.domain.auth.entity.AuthorizeUserVerification;
import com.epay.domain.auth.repository.AuthorizeUserVerificationRepository;


@Service
public class AuthorizeUserVerificationService implements IAuthorizeUserVerificationService {

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
