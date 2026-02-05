package com.pesco.wallet_service.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {

    @Value("${history-service.base-url}")
    private String historyServiceBaseUrl;

    @Value("${authentication-service.base-url}")
    private String authServiceBaseUrl;

    @Value("${notification-service.base-url}")
    private String notificationServiceBaseUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure TcpClient with timeouts
        TcpClient tcpClient = TcpClient.create()
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS));
                });

        // Use DefaultAddressResolverGroup to resolve DNS issues
        @SuppressWarnings("deprecation")
        HttpClient httpClient = HttpClient.from(tcpClient)
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(Duration.ofSeconds(15));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    public WebClient historyServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(historyServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient authServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(authServiceBaseUrl) 
                .build();
    }

     @Bean
    public WebClient notificationServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(notificationServiceBaseUrl) 
                .build();
    }

}
