package com.team01.deokhugam.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String TEST_EMAIL = "test@email.com";
  private static final String TEST_NICKNAME = "테스트닉네임";
  private static final String TEST_PASSWORD = "Password1!";
  private static final String ENCODED_PASSWORD = "encoded-password-hash";

  // 가짜 객체 생성
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  // 테스트 대상 객체 생성 + @Mock 객체를 자동 주입
  @InjectMocks
  private UserService userService;

  private UserRegisterRequest userRegisterRequest;
  private User savedUser;

  // 각 @Test 메서드 실행 전에 매번 호출 (공통 테스트 데이터 관리)
  @BeforeEach
  void setUp() {
    userRegisterRequest = new UserRegisterRequest(
        TEST_EMAIL,
        TEST_NICKNAME,
        TEST_PASSWORD
    );

    // 저장 이후 상태 = 인코딩된 비밀번호를 가진 User
    savedUser = new User(TEST_EMAIL, TEST_NICKNAME, ENCODED_PASSWORD);
  }

  @Test
  @DisplayName("회원가입 성공 - 중복되지 않은 이메일일 때")
  void register_Success() {
    // given
    given(userRepository.existsByEmailAndDeletedAtIsNull(TEST_EMAIL)).willReturn(false);
    given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PASSWORD);
    given(userRepository.save(any(User.class))).willReturn(savedUser);

    // when
    UserDto result = userService.register(userRegisterRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo(TEST_EMAIL);
    assertThat(result.nickname()).isEqualTo(TEST_NICKNAME);

    // 호출 검증
    verify(userRepository).existsByEmailAndDeletedAtIsNull(TEST_EMAIL);
    verify(passwordEncoder).encode(TEST_PASSWORD);

    // save에 전달된 User의 비밀번호가 인코딩된 값인지 확인

    // ArgumentCaptor:
    //   - 모킹된 객체의 메서드가 호출될 때 전달된 인자를 캡처하여 사후에 검증할 때 사용
    //   - 메서드에 전달된 객체의 내부 필드 값이 예상과 일치하는가를 확인할 때 사용
    // User 타입의 인자를 캡처할 수 있는 캡처 객체를 생성
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    // captor.capture()가 호출 시점에 save()에 전달된 User 객체를 캡처
    verify(userRepository).save(captor.capture());
    // 인코딩 되어 저장되는지 검증
    assertThat(captor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 이메일일 때")
  void register_Fail_DuplicateEmail() {
    // given
    given(userRepository.existsByEmailAndDeletedAtIsNull(TEST_EMAIL)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> userService.register(userRegisterRequest))
        .isInstanceOf(EmailAlreadyExistsException.class);

    // 중복이면 인코딩/저장은 실행되지 않아야 함
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }
}
