package com.team01.deokhugam.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.global.exception.user.EmailAlreadyExistsException;
import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String TEST_EMAIL = "test@email.com";
  private static final String TEST_NICKNAME = "테스트닉네임";
  private static final String TEST_PASSWORD = "Password1!";

  // 가짜 객체 생성
  @Mock
  private UserRepository userRepository;

  // 테스트 대상 객체 생성 + @Mock 객체를 자동 주입
  @InjectMocks
  private UserService userService;

  private UserRegisterRequest userRegisterRequest;
  private User user;

  // 각 @Test 메서드 실행 전에 매번 호출 (공통 테스트 데이터 관리)
  @BeforeEach
  void setUp() {
    userRegisterRequest = new UserRegisterRequest(
        TEST_EMAIL,
        TEST_NICKNAME,
        TEST_PASSWORD
    );

    user = new User(
        userRegisterRequest.email(),
        userRegisterRequest.nickname(),
        userRegisterRequest.password()
    );
  }

  @Test
  @DisplayName("회원가입 성공 - 중복되지 않은 이메일일 때")
  void register_Success() {
    // given
    given(userRepository.existsByEmailAndDeletedAtIsNull(TEST_EMAIL)).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    UserDto result = userService.register(userRegisterRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo(TEST_EMAIL);
    assertThat(result.nickname()).isEqualTo(TEST_NICKNAME);

    verify(userRepository).existsByEmailAndDeletedAtIsNull(TEST_EMAIL);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 이메일일 때")
  void register_Fail_DuplicateEmail() {
    // given
    given(userRepository.existsByEmailAndDeletedAtIsNull(TEST_EMAIL)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> userService.register(userRegisterRequest))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any(User.class));
  }
}
