package com.team01.deokhugam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "deokhugam.naver.client.id=test-id",
    "deokhugam.naver.client.secret=test-secret"
})
class DeokhugamApplicationTests {

	@Test
	void contextLoads() {
	}

}
