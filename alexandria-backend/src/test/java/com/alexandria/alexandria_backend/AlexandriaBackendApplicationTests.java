package com.alexandria.alexandria_backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // <- adicionar isso

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
class AlexandriaBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
