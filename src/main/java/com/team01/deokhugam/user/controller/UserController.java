package com.team01.deokhugam.user.controller;

import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /// POST - /api/users - 회원가입
  @PostMapping
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
    log.info("회원가입 요청: email={}", request.email());
    UserDto result = userService.register(request);
    log.info("회원가입 완료: userId={}", result.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(result);
  }

  /// POST - /api/users/login - 로그인

  /// GET - /api/users/{userId} - 사용자 정보 조회

  /// DELETE - /api/users/{userId} - 사용자 논리 삭제

  /// PATCH - /api/users/{userId} - 사용자 정보 수정

  /// GET - /api/users/power - 파워 유저 목록 조회

  /// DELETE - /api/users/{userId}/hard - 사용자 물리 삭제
}
