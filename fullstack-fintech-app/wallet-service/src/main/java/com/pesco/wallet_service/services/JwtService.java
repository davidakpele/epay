package com.pesco.wallet_service.services;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    Claims extractAllClaims(String token);
    List<String> extractRoles(String token);

    boolean isTokenValid(String token);
    boolean isTokenValid(String token, UserDetails userDetails);

    String generateToken(UserDetails userDetails);
    String generateToken(Map<String, Object> claims, UserDetails userDetails);
}