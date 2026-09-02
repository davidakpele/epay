package com.epay.common.config.services;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.epay.common.config.components.JwtProperties;
import com.epay.common.config.interfaces.IJwtService;
import com.epay.common.exception.JwtAuthenticationException;
import com.epay.domain.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService implements IJwtService {

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = getSigningKey();
    }

    @SuppressWarnings("null")
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public UUID extractUserUuid(String token) {
        String sub = extractUsername(token);
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("Invalid UUID in sub claim",
                    HttpStatus.UNAUTHORIZED, "INVALID_SUB_CLAIM");
        }
    }

    @Override
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        if (val instanceof Number n) return n.longValue();
        return null;
    }

    @Override
    public List<String> extractRoles(String token) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) extractAllClaims(token).get("roles");
        return roles != null ? roles : List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Object val = extractAllClaims(token).get("permissions");
        if (val instanceof List<?> list) return (List<String>) list;
        return List.of();
    }

    @Override
    public String extractAccountStatus(String token) {
        Object val = extractAllClaims(token).get("accountStatus");
        return val != null ? val.toString() : null;
    }

    @Override
    public String extractAcr(String token) {
        Object val = extractAllClaims(token).get("acr");
        return val != null ? val.toString() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractAmr(String token) {
        Object val = extractAllClaims(token).get("amr");
        if (val instanceof List<?> list) return (List<String>) list;
        return List.of();
    }

    @Override
    public String extractSessionId(String token) {
        return extractNestedString(token, "session", "id");
    }

    @Override
    public String extractTokenId(String token) {
        return extractNestedString(token, "session", "token_id");
    }

    @Override
    public String extractTokenType(String token) {
        return extractNestedString(token, "token", "type");
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    @Override
    public Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtAuthenticationException("Token is null or empty",
                    HttpStatus.UNAUTHORIZED, "EMPTY_TOKEN");
        }

        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtAuthenticationException("Invalid JWT structure",
                    HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_STRUCTURE");
        }

        for (String part : parts) {
            if (part.isEmpty()) {
                throw new JwtAuthenticationException("Invalid JWT: empty part",
                        HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT");
            }
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token is expired",
                    HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", e);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Invalid JWT token format",
                    HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT", e);
        } catch (SignatureException e) {
            throw new JwtAuthenticationException("JWT signature does not match",
                    HttpStatus.UNAUTHORIZED, "INVALID_SIGNATURE", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("JWT token is unsupported",
                    HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", e);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT claims string is empty",
                    HttpStatus.BAD_REQUEST, "EMPTY_TOKEN", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT validation failed",
                    HttpStatus.UNAUTHORIZED, "TOKEN_VALIDATION_FAILED", e);
        }
    }

    @Override
    public String generateToken(UserDetails userDetails,
                                String sessionId,
                                String tokenId,
                                List<String> authMethods) {
        try {
            List<String> roles = extractRoleNames(userDetails);
            String assurance = resolveAssurance(authMethods);

            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("jti", tokenId);
            claims.put("sid", sessionId);

            claims.put("principal", Map.of(
                "auth", buildPrincipalAuth(roles)
            ));
            claims.put("roles", roles);
            claims.put("permissions", buildPermissions(roles));

            if (userDetails instanceof User user) {
                Long userId = user.getId();
                log.debug("JWT generateToken: resolved userId={} for principal={}", userId, userDetails.getUsername());

                claims.put("userId",        userId);
                claims.put("accountId",     buildAccountId(userId));
                claims.put("accountStatus", resolveAccountStatus(user));
                claims.put("kycLevel",      resolveKycLevel(user.getKycTier()));
            } else {
                log.warn("JWT generateToken: userDetails is not instance of User (actual class={}), userId claim will be absent",
                        userDetails.getClass().getName());
            }

            claims.put("amr", buildAmr(authMethods));
            claims.put("acr", resolveAcr(authMethods));
            claims.put("auth_time", Instant.now().getEpochSecond());

            claims.put("auth", Map.of(
                "methods",  authMethods,
                "assurance", assurance
            ));
            claims.put("session", Map.of(
                "id",       sessionId,
                "token_id", tokenId
            ));
            claims.put("token", Map.of("type", "ACCESS"));

            log.debug("JWT generateToken: final claim keys={}", claims.keySet());

            String sub    = resolveSubject(userDetails);
            Instant now   = Instant.now();
            Instant expiry = now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);

            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setHeaderParam("alg", "HS256")
                    .setClaims(claims)
                    .setSubject(sub)
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .setNotBefore(Date.from(now))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (InvalidKeyException e) {
            throw new JwtAuthenticationException("Failed to generate JWT token",
                    HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_GENERATION_ERROR", e);
        }
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails,
                                       String sessionId,
                                       String tokenId) {
        try {
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("session", Map.of(
                "id",       sessionId,
                "token_id", tokenId
            ));
            claims.put("token", Map.of("type", "REFRESH"));

            String sub = resolveSubject(userDetails);
            Instant now = Instant.now();
            Instant expiry = now.plus(jwtProperties.getRefreshExpirationDays(), ChronoUnit.DAYS);

            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setHeaderParam("alg", "HS256")
                    .setClaims(claims)
                    .setSubject(sub)
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .setNotBefore(Date.from(now))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (InvalidKeyException e) {
            throw new JwtAuthenticationException("Failed to generate refresh token",
                    HttpStatus.INTERNAL_SERVER_ERROR, "REFRESH_TOKEN_GENERATION_ERROR", e);
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String sub = extractUsername(token);
            final String expected = resolveSubject(userDetails);
            return sub.equals(expected) &&
                   !isTokenExpired(token) &&
                   validateTokenClaims(token);
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean validateTokenClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);

            String expectedIssuer = jwtProperties.getIssuer();
            if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
                return false;
            }

            String expectedAudience = jwtProperties.getAudience();
            if (expectedAudience != null && !expectedAudience.equals(claims.getAudience())) {
                return false;
            }

            return !(claims.getNotBefore() != null && claims.getNotBefore().after(new Date()));
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }

    @SuppressWarnings("null")
    @Override
    public Date getExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return getExpirationDate(token).before(new Date());
        } catch (JwtAuthenticationException e) {
            return true;
        }
    }

    @Override
    public long getRemainingValidity(String token) {
        try {
            long remaining = getExpirationDate(token).getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (JwtAuthenticationException e) {
            return 0;
        }
    }

    private Key getSigningKey() {
        try {
            String secret = jwtProperties.getSecret();
            if (secret == null || secret.trim().isEmpty()) {
                throw new IllegalStateException("JWT secret key is not configured");
            }
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (DecodingException | WeakKeyException e) {
            throw new IllegalStateException("Failed to initialize JWT signing key", e);
        }
    }

    private String resolveSubject(UserDetails userDetails) {
        if (userDetails instanceof User user && user.getUserUuid() != null) {
            return user.getUserUuid().toString();
        }
        return userDetails.getUsername();
    }

    private List<String> extractRoleNames(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildPrincipalAuth(List<String> roles) {
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("admin",                roles.contains("ADMIN") || roles.contains("CUSTOMER_SERVICE") || roles.contains("EDITOR"));
        auth.put("user",                 roles.contains("USER"));
        auth.put("system_admin",         roles.contains("SUPER_USER"));
        auth.put("external_partner_api", false);
        return auth;
    }

    private List<String> buildPermissions(List<String> roles) {
        if (roles.contains("SUPER_USER")) {
            return List.of(
                "payment:create", "payment:read", "payment:update", "payment:delete",
                "wallet:read",    "wallet:update", "wallet:freeze",
                "user:read",      "user:update",   "user:delete",
                "admin:access",   "crypto:deposit", "crypto:withdraw"
            );
        }
        if (roles.contains("ADMIN")) {
            return List.of(
                "payment:create", "payment:read", "payment:update",
                "wallet:read",    "wallet:update", "wallet:freeze",
                "user:read",      "user:update",
                "admin:access",   "crypto:deposit"
            );
        }
        if (roles.contains("CUSTOMER_SERVICE")) {
            return List.of(
                "payment:read",
                "wallet:read", "wallet:freeze",
                "user:read",
                "admin:access"
            );
        }
        if (roles.contains("EDITOR")) {
            return List.of(
                "payment:read",
                "wallet:read",
                "user:read",
                "admin:access"
            );
        }
        return List.of(
            "payment:create", "payment:read",
            "wallet:read",
            "crypto:deposit"
        );
    }

    private String resolveAssurance(List<String> authMethods) {
        if (authMethods.contains("MFA")) return "MFA";
        if (authMethods.contains("PASSWORD")) return "PASSWORD";
        return authMethods.isEmpty() ? "NONE" : authMethods.get(0);
    }

    private List<String> buildAmr(List<String> authMethods) {
        List<String> amr = new java.util.ArrayList<>();
        for (String method : authMethods) {
            switch (method.toUpperCase()) {
                case "PASSWORD" -> amr.add("pwd");
                case "MFA"      -> amr.add("mfa");
                case "OTP"      -> amr.add("otp");
                case "BIOMETRIC"-> amr.add("bio");
                default         -> amr.add(method.toLowerCase());
            }
        }
        return amr;
    }

    private String resolveAcr(List<String> authMethods) {
        if (authMethods.contains("MFA")) return "urn:epay:auth:mfa";
        return "urn:epay:auth:password";
    }

    private String buildAccountId(Long userId) {
        if (userId == null) return "ACC_UNKNOWN";
        String encoded = Long.toString(userId, 36).toUpperCase();
        return "ACC_" + String.format("%8s", encoded).replace(' ', '0');
    }

    private String resolveAccountStatus(User user) {
        if (!user.isEnabled())      return "INACTIVE";
        if (user.isAccountLocked()) return "LOCKED";
        return "ACTIVE";
    }

    private String resolveKycLevel(com.epay.domain.auth.enums.KycTier tier) {
        if (tier == null) return "LEVEL_0";
        return switch (tier) {
            case TIER_1 -> "LEVEL_1";
            case TIER_2 -> "LEVEL_2";
            case TIER_3 -> "LEVEL_3";
        };
    }

    @SuppressWarnings("unchecked")
    private String extractNestedString(String token, String parent, String key) {
        Claims claims = extractAllClaims(token);
        Object parentObj = claims.get(parent);
        if (parentObj instanceof Map) {
            Object value = ((Map<String, Object>) parentObj).get(key);
            return value != null ? value.toString() : null;
        }
        return null;
    }
}   