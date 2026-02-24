package com.pesco.wallet_service.util;

import java.util.Date;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import com.pesco.wallet_service.handler.JwtServiceImplementations;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Component
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    
    private final JwtServiceImplementations jwtService;
    
    public AuthHandshakeInterceptor(JwtServiceImplementations jwtService) {
        this.jwtService = jwtService;
    }
    

    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, 
                                   Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            
            String token = extractToken(httpRequest);
            
            if (token != null && !isTokenExpired(token)) {
                String username = jwtService.extractUsername(token);
                attributes.put("user", username);
                
                String userId = httpRequest.getParameter("userId");
                if (userId != null) {
                    attributes.put("userId", userId);
                }
                
                return true;
            } else {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }
        
        return false;
    }
    

    public void afterHandshake(ServerHttpRequest request, 
                               ServerHttpResponse response, 
                               WebSocketHandler wsHandler, 
                               Exception exception) {
    }
    
    private String extractToken(HttpServletRequest request) {
        String token = request.getParameter("token");
        
        if (token == null) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            }
        }
        
        return token;
    }
    
    private boolean isTokenExpired(String token) {
        try {
            return jwtService.extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}