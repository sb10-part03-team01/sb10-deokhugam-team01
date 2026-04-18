package com.team01.deokhugam.user.controller;

import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserLoginRequest;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.dto.UserUpdateRequest;
import com.team01.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /// POST - /api/users - 회원가입
  @PostMapping
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
    log.info("회원가입 요청 수신");
    UserDto result = userService.register(request);
    log.info("회원가입 완료");
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(result);
  }

  /// POST - /api/users/login - 로그인
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
    log.info("로그인 요청 수신");
    UserDto result = userService.login(request);
    log.info("로그인 성공");
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(result);
  }

  /// GET - /api/users/{userId} - 사용자 정보 조회
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
    log.info("사용자 조회 요청");
    return ResponseEntity.ok(userService.getUser(userId));
  }

  /// DELETE - /api/users/{userId} - 사용자 논리 삭제
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    log.info("사용자 논리 삭제 요청");
    userService.deleteUser(userId);
    log.info("사용자 논리 삭제 완료");
    return ResponseEntity
        .noContent()
        .build();
  }

  /// PATCH - /api/users/{userId} - 사용자 정보 수정
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID userId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    log.info("사용자 정보 수정 요청");
    UserDto result = userService.updateUser(userId, request);
    log.info("사용자 정보 수정 완료");
    return ResponseEntity.ok(result);
  }

  /// GET - /api/users/power - 파워 유저 목록 조회

  /// DELETE - /api/users/{userId}/hard - 사용자 물리 삭제
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> permanentDeleteUser(@PathVariable UUID userId) {
    log.warn("사용자 물리 삭제 요청");
    userService.permanentDeleteUser(userId);
    log.warn("사용자 물리 삭제 완료");
    return ResponseEntity
        .noContent()
        .build();
  }
}
