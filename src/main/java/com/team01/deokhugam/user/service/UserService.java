package com.team01.deokhugam.user.service;

import com.team01.deokhugam.global.exception.user.EmailAlreadyExistsException;
import com.team01.deokhugam.global.exception.user.LoginFailedException;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.global.util.PiiMasker;
import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserLoginRequest;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.dto.UserUpdateRequest;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public UserDto register(UserRegisterRequest request) {
    log.debug("회원가입 처리 시작: email={}", PiiMasker.maskEmail(request.email()));
    if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
      throw new EmailAlreadyExistsException();
    }
    String encodedPassword = passwordEncoder.encode(request.password());
    User user = new User(request.email(), request.nickname(), encodedPassword);
    User savedUser = userRepository.save(user);
    log.debug("회원가입 처리 완료: userId={}, email={}",
        savedUser.getId(), PiiMasker.maskEmail(savedUser.getEmail()));
    return UserDto.from(savedUser);
  }

  @Transactional(readOnly = true)
  public UserDto login(UserLoginRequest request) {
    log.debug("로그인 처리 시작: email={}", request.email());
    User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> new LoginFailedException());
    /*
    request.password() - 사용자가 로그인 요청에 보낸 평문 비밀번호
    user.getPassword() - DB에 저장된 BCrypt 해시값
    * salt - 비밀번호를 해싱할 때 함께 섞어넣는 랜덤한 문자열

    - DB에 저장된 해시값에서 BCrypt의 salt를 추출
    - 해당 salt로 입력받은 평문 비밀번호를 동일한 방식으로 해싱
    - 해싱 결과와 저장된 해시를 상수 시간 비교 (타이밍 공격 방지)
      - 중간에 달라도 멈추지 않고, 항상 전체 길이를 끝까지 비교
      - 공격자가 해시값의 앞부분이 얼마나 맞았는지 시간으로 추측할 수 없게 하기 위함
     */
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new LoginFailedException();
    }
    log.debug("로그인 처리 완료: userId={}", user.getId());
    return UserDto.from(user);
  }

  @Transactional(readOnly = true)
  public UserDto getUser(UUID userId) {
    log.debug("사용자 조회: userId={}", userId);
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    return UserDto.from(user);
  }

  @Transactional
  public UserDto updateUser(UUID userId, UserUpdateRequest request) {
    log.debug("사용자 정보 수정 시작: userId={}, nickname={}", userId, request.nickname());
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    user.updateNickname(request.nickname());
    log.debug("사용자 정보 수정 완료: userId={}", userId);
    return UserDto.from(user);
    // JPA - Dirty Checking 으로 DB에 수정사항 저장됨
  }

  @Transactional
  public void deleteUser(UUID userId) {
    log.debug("사용자 논리 삭제 시작: userId={}", userId);
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    user.softDelete();
    log.info("사용자 논리 삭제 완료: userId={}", userId);
    // JPA - Dirty Checking 으로 DB에 삭제사항 저장됨
  }

  @Transactional
  public void permanentDeleteUser(UUID userId) {
    log.warn("사용자 물리 삭제 시작: userId={}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    userRepository.delete(user);
    log.warn("사용자 물리 삭제 완료: userId={}", userId);
  }

}
