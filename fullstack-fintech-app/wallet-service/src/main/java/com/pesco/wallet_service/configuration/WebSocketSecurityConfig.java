package com.pesco.wallet_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        return (authentication, message) -> {
            return authentication.get().isAuthenticated()
                    ? new AuthorizationDecision(true)
                    : new AuthorizationDecision(false);
        };
    }

    @Bean
    public AuthorizationChannelInterceptor authorizationChannelInterceptor(
            AuthorizationManager<Message<?>> authorizationManager) {
        return new AuthorizationChannelInterceptor(authorizationManager);
    }
}