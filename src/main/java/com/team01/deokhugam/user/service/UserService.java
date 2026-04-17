package com.team01.deokhugam.user.service;

import com.team01.deokhugam.global.exception.user.EmailAlreadyExistsException;
import com.team01.deokhugam.global.util.PiiMasker;
import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
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
}
