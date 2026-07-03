package com.epay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.epay")
@EnableJpaRepositories(basePackages = "com.epay")
@EnableScheduling
@EnableAsync
public class EpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpayApplication.class, args);
    }

}
