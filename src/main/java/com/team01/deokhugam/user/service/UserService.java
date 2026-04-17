package com.team01.deokhugam.user.service;

import com.team01.deokhugam.global.exception.user.EmailAlreadyExistsException;
import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public UserDto register(UserRegisterRequest request) {
    log.debug("회원가입 처리 시작: email={}", request.email());
    if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
      throw new EmailAlreadyExistsException(request.email());
    }
    User user = new User(request.email(), request.nickname(), request.password());
    User savedUser = userRepository.save(user);
    log.debug("회원가입 처리 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
    return UserDto.from(savedUser);
  }
}
