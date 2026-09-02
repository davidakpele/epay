package com.epay.common.config.interfaces;

import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {

    String extractUsername(String token);

    UUID extractUserUuid(String token);
    Long extractUserId(String token);
    List<String> extractRoles(String token);

    /** Returns the {@code permissions} list claim. */
    List<String> extractPermissions(String token);

    /** Returns the {@code accountStatus} claim: ACTIVE, INACTIVE, or LOCKED. */
    String extractAccountStatus(String token);

    /** Returns the {@code acr} claim (Authentication Context Class Reference). */
    String extractAcr(String token);

    /** Returns the {@code amr} list claim (Authentication Methods References). */
    List<String> extractAmr(String token);

    String extractSessionId(String token);

    String extractTokenId(String token);

    String extractTokenType(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    Claims extractAllClaims(String token);

    String generateToken(UserDetails userDetails, String sessionId, String tokenId, List<String> authMethods);

    String generateRefreshToken(UserDetails userDetails, String sessionId, String tokenId);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean validateToken(String token);

    boolean validateTokenClaims(String token);

    Date getExpirationDate(String token);

    boolean isTokenExpired(String token);

    long getRemainingValidity(String token);
}

