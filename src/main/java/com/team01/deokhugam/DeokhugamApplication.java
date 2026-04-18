package com.team01.deokhugam;

import com.team01.deokhugam.user.config.UserCleanupProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// Spring의 스케줄링 기능을 활성화 - @Scheduled가 붙은 메서드가 동작하기 위해 필수
@EnableScheduling
// 외부 설정 파일의 값을 자바 객체로 바인딩하는 기능 활성화
// UserCleanUpProperties 클래스를 Spring Bean으로 등록
@EnableConfigurationProperties(UserCleanupProperties.class)
public class DeokhugamApplication {

  public static void main(String[] args) {
    SpringApplication.run(DeokhugamApplication.class, args);
  }
}
