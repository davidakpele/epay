package com.payrix.administrator.services;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.payrix.administrator.exceptions.JwtAuthenticationException;
import com.payrix.administrator.utils.JwtProperties;

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

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = getSigningKey();
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to extract username from token",
                HttpStatus.UNAUTHORIZED, e);
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("userId", Long.class);
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to extract user ID from token",
                HttpStatus.UNAUTHORIZED, e);
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            return roles != null ? roles : List.of();
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to extract roles from token",
                HttpStatus.UNAUTHORIZED, e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to extract claim from token",
                HttpStatus.UNAUTHORIZED, e);
        }
    }

    public Claims extractAllClaims(String token) {
        try {
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

            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Invalid JWT token format",
                HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_FORMAT", e);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token is expired",
                HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("JWT token is unsupported",
                HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", e);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT claims string is empty",
                HttpStatus.BAD_REQUEST, "EMPTY_TOKEN", e);
        } catch (SignatureException e) {
            throw new JwtAuthenticationException("JWT signature does not match",
                HttpStatus.UNAUTHORIZED, "INVALID_SIGNATURE", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT validation failed",
                HttpStatus.UNAUTHORIZED, "TOKEN_VALIDATION_FAILED", e);
        } catch (JwtAuthenticationException e) {
            throw new JwtAuthenticationException("Failed to parse JWT token",
                HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_PARSING_ERROR", e);
        }
    }

    private Key getSigningKey() {
        try {
            String secretKey = jwtProperties.getSecretKey();
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalStateException("JWT secret key is not configured");
            }
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid JWT secret key format. Must be base64 encoded.", e);
        } catch (DecodingException | WeakKeyException | IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize JWT signing key", e);
        }
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        try {
            Map<String, Object> claims = new HashMap<>();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
            claims.put("roles", roles);
            claims.put("userId", userId);
            claims.put("iss", jwtProperties.getIssuer());
            claims.put("aud", jwtProperties.getAudience());

            Instant now = Instant.now();
            Instant expiry = now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);

            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setHeaderParam("alg", "HS256")
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
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

    public String generateRefreshToken(UserDetails userDetails) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(jwtProperties.getRefreshExpirationDays(), ChronoUnit.DAYS);
            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setHeaderParam("alg", "HS256")
                    .setSubject(userDetails.getUsername())
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .setNotBefore(Date.from(now))
                    .claim("tokenType", "refresh")
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
        } catch (InvalidKeyException e) {
            throw new JwtAuthenticationException("Failed to generate refresh token",
                HttpStatus.INTERNAL_SERVER_ERROR, "REFRESH_TOKEN_GENERATION_ERROR", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) &&
                   !isTokenExpired(token) &&
                   validateTokenClaims(token);
        } catch (JwtAuthenticationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) throws JwtException {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

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

            if (claims.getNotBefore() != null && claims.getNotBefore().after(new Date())) {
                return false;
            }

            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to get expiration date from token",
                HttpStatus.UNAUTHORIZED, "EXPIRATION_EXTRACTION_ERROR", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (JwtAuthenticationException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public long getRemainingValidity(String token) {
        try {
            Date expiration = getExpirationDate(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (JwtAuthenticationException e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            return tokenType != null ? tokenType : "access";
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to extract token type",
                HttpStatus.UNAUTHORIZED, "TOKEN_TYPE_EXTRACTION_ERROR", e);
        }
    }
}