package com.epay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Single Spring Boot entry point for the ePay platform.
 * Component/entity/repository scanning is rooted at the base package so that
 * every feature module (auth, wallet, deposit, withdraw, virtual_card, escrow,
 * beneficiary, blacklist, maintenance, savings, investment, notification,
 * history, admin) plus the shared domain and common modules are picked up
 * automatically, without needing to be declared individually here.
 */
@SpringBootApplication(scanBasePackages = "com.epay")
@EntityScan(basePackages = "com.epay")
@EnableJpaRepositories(basePackages = "com.epay")
@EnableScheduling
@EnableAsync
public class EpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpayApplication.class, args);
    }

}
