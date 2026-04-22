package com.team01.deokhugam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 테스트 실행 시 test 프로파일을 활성화
@ActiveProfiles("test")
@SpringBootTest(properties = {
    "deokhugam.naver.client.id=test-id",
    "deokhugam.naver.client.secret=test-secret"
})
class DeokhugamApplicationTests {

  @Test
  void contextLoads() {
  }

}
