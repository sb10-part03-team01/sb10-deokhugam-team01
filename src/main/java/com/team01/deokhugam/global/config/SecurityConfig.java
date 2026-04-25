package com.team01.deokhugam.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

  /*
  BCrypt: 단방향 해시 알고리즘. 같은 비밀번호를 넣어도 매번 다른 해시값이 나옴.
          그래서 DB가 유출되어도 원본 비밀번호를 역추적하기 어려움
  // 회원가입 시 - 암호화해서 저장
  String encoded = passwordEncoder.encode("rawPassword");
  // 로그인 시 - 입력값과 저장된 해시 비교
  passwordEncoder.matches("rawPassword", encoded);
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
