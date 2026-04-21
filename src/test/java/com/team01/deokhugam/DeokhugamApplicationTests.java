package com.team01.deokhugam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "naver.client.id=test-id",
    "naver.client.secret=test-secret"
})
class DeokhugamApplicationTests {

	@Test
	void contextLoads() {
	}

}
