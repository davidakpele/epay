package com.example.admin_api_service.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnProperty(
    name = "spring.jpa.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class JpaConfig {

    @Bean
    @Primary  // ← resolves the NoUniqueBeanDefinitionException
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}