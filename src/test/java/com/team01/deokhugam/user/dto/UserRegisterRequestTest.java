package com.team01.deokhugam.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserRegisterRequest 비밀번호 검증")
class UserRegisterRequestTest {

  // 클래스 패스에서 Bean Validation 구현체를 탐색해 Validator를 만들어 줌
  private static ValidatorFactory factory;
  // 실제로 검증을 수행하는 객체. 객체를 받아서 어노테이션을 읽고, 위반사항을 Set<ConstraintViolation<T>>으로 반환
  private static Validator validator;

  private static final String VALID_EMAIL = "test@example.com";
  private static final String VALID_NICKNAME = "닉네임";

  @BeforeAll
  static void setUp() {
    // 구현체 자동 탐색 -> 기본 설정 적용 -> 표현식 언어 엔진 준비
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    // 팩토리 내부 리소스(스레드/캐시) 정리. 누수 방지용.
    factory.close();
  }

  // password 필드만 골라 검증하고 위반 목록을 반환.
  private Set<ConstraintViolation<UserRegisterRequest>> validatePassword(String password) {
    UserRegisterRequest request = new UserRegisterRequest(VALID_EMAIL, VALID_NICKNAME, password);
    return validator.validateProperty(request, "password"); // 객체의 특정 필드 하나만 골라서 검증
  }

  @Test
  @DisplayName("성공 - 영문/숫자/특수문자 포함 8~20자")
  void validPassword() {
    // given
    Set<ConstraintViolation<UserRegisterRequest>> violations = validatePassword("Password1!~");

    // when & then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("실패 - 특수문자 누락")
  void invalidPassword() {
    // given
    Set<ConstraintViolation<UserRegisterRequest>> violations = validatePassword("Password1");

    // when & then
    assertThat(violations).isNotEmpty();
  }
}
