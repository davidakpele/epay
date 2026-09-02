package com.epay.common.config.security;

import com.epay.common.config.interfaces.IJwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Slf4j
@Component
@RequestScope
public class JwtClaimsHolder {

    private final IJwtService       jwtService;
    private final HttpServletRequest request;

    private boolean   parsed       = false;
    private String    rawToken     = null;

    private String       sub           = null;
    private Long         userId        = null;
    private String       sessionId     = null;
    private String       tokenId       = null;
    private String       tokenType     = null;
    private String       accountStatus = null;
    private String       acr           = null;
    private List<String> roles         = List.of();
    private List<String> permissions   = List.of();
    private List<String> amr           = List.of();

    public JwtClaimsHolder(IJwtService jwtService, HttpServletRequest request) {
        this.jwtService = jwtService;
        this.request    = request;
    }

    public boolean isPresent()      { parse(); return rawToken != null; }

    public String       getSub()           { parse(); return sub; }
    public Long         getUserId()        { parse(); return userId; }
    public String       getSessionId()     { parse(); return sessionId; }
    public String       getTokenId()       { parse(); return tokenId; }
    public String       getTokenType()     { parse(); return tokenType; }
    public String       getAccountStatus() { parse(); return accountStatus; }
    public String       getAcr()           { parse(); return acr; }
    public List<String> getRoles()         { parse(); return roles; }
    public List<String> getPermissions()   { parse(); return permissions; }
    public List<String> getAmr()           { parse(); return amr; }

    public boolean hasPermission(String permission) {
        parse();
        return permission != null && permissions.contains(permission);
    }

    public boolean isAccountActive() {
        parse();
        return "ACTIVE".equals(accountStatus);
    }

    public boolean isAccessToken() {
        parse();
        return tokenType == null || "ACCESS".equalsIgnoreCase(tokenType);
    }

    public boolean isMfaAuthenticated() {
        parse();
        return "urn:epay:auth:mfa".equals(acr);
    }

    public String getRawToken() {
        parse();
        return rawToken;
    }


    private void parse() {
        if (parsed) return;
        parsed = true;

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return;

        String token = header.substring(7).trim();
        if (token.isBlank()) return;

        try {
            this.rawToken     = token;
            this.sub          = jwtService.extractUsername(token);
            this.userId       = jwtService.extractUserId(token);
            this.sessionId    = jwtService.extractSessionId(token);
            this.tokenId      = jwtService.extractTokenId(token);
            this.tokenType    = jwtService.extractTokenType(token);
            this.accountStatus= jwtService.extractAccountStatus(token);
            this.acr          = jwtService.extractAcr(token);
            this.roles        = jwtService.extractRoles(token);
            this.permissions  = jwtService.extractPermissions(token);
            this.amr          = jwtService.extractAmr(token);
        } catch (Exception e) {
            this.rawToken = null;
            log.debug("[JwtClaimsHolder] Could not parse token: {}", e.getMessage());
        }
    }
}
