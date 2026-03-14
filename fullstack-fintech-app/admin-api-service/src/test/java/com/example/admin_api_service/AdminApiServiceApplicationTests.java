package com.example.admin_api_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.enabled=false"
}) 
class AdminApiServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
