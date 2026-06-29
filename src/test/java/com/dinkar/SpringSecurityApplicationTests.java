package com.dinkar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.app.demo.SpringSecurityApplication;

@SpringBootTest(classes = SpringSecurityApplication.class)
// @ActiveProfiles("test")
class SpringSecurityApplicationTests {

	@Test
	void contextLoads() {
	}

}