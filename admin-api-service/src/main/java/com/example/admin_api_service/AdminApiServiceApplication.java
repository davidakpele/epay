package com.example.admin_api_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AdminApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminApiServiceApplication.class, args);
	}

}
