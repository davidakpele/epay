package pesco.example.virtual_card_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
public class VirtualCardServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(VirtualCardServiceApplication.class, args);
	}

}
