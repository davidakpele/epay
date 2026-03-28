package com.example.auth_user_service.configurations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {

    @Value("${wallet-service.base-url}")
    private String walletServiceBaseUrl;

    @Value("${notification-service.base-url}")
    private String notificationServiceBaseUrl;

    @Value("${administrator-service.base-url}")
    private String administratorServiceBaseUrl;

    @SuppressWarnings("deprecation")
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Increase memory limits for WebClient
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024); // 16MB
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();

        // Configure TcpClient with timeouts
        TcpClient tcpClient = TcpClient.create()
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS));
                });

        HttpClient httpClient = HttpClient.from(tcpClient)
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(Duration.ofSeconds(30))
                .followRedirect(true);

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/json");
    }

    @Bean
    public WebClient walletServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(walletServiceBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient notificationServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clone()
                .baseUrl(notificationServiceBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient administratorServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clone()
                .baseUrl(administratorServiceBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}