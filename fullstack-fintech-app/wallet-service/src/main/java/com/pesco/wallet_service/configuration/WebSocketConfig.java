package com.pesco.wallet_service.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.pesco.wallet_service.broker.WalletServiceSocketBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

     private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final WalletServiceSocketBroker walletServiceSocketBroker;

    public WebSocketConfig(WalletServiceSocketBroker walletServiceSocketBroker) {
        this.walletServiceSocketBroker = walletServiceSocketBroker;
        log.info("WebSocketConfig initialized");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handler at /ws/wallet");
        registry.addHandler(walletServiceSocketBroker, "/ws/wallet")
                .setAllowedOriginPatterns("*");
    }
}