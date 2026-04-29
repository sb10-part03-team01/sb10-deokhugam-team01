package com.team01.deokhugam.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.dashboard.poweruser.service.PowerUserService;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.exception.user.EmailAlreadyExistsException;
import com.team01.deokhugam.global.exception.user.LoginFailedException;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.global.exception.user.UserNotSoftDeletedException;
import com.team01.deokhugam.user.dto.UserDto;
import com.team01.deokhugam.user.dto.UserLoginRequest;
import com.team01.deokhugam.user.dto.UserRegisterRequest;
import com.team01.deokhugam.user.dto.UserUpdateRequest;
import com.team01.deokhugam.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Spring MVC 계층만 로드하는 슬라이스 테스트
@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerTest {

  // 테스트 공통 픽스처: 모든 케이스에서 동일하게 사용되는 정상 입력값
  private static final String EMAIL = "test@email.com";
  private static final String NICKNAME = "테스터";
  private static final String PASSWORD = "Password1!";

  // MockMvc: Spring MVC 테스트를 위한 도구
  @Autowired
  private MockMvc mockMvc;

  // ObjectMapper: Jackson의 JSON 직렬화/역직렬화 도구
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean // 가짜 객체 — 모든 메서드가 기본적으로 null/0/false를 반환
  private UserService userService;

  @MockitoBean
  private PowerUserService powerUserService;

  @Nested
  @DisplayName("POST /api/users - 회원가입")
  class Register {

    @Test
    @DisplayName("성공(201) - 회원가입 성공")
    void should_ReturnCreated_When_ValidRequest() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, PASSWORD);
      UserDto response = new UserDto(userId, EMAIL, NICKNAME, OffsetDateTime.now());
      given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))) // 자바 객체를 JSON 문자열로 변환
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.email").value(EMAIL))
          .andExpect(jsonPath("$.nickname").value(NICKNAME));

      verify(userService).register(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 잘못된 입력(일력값 검증 실패) - 올바르지 않은 이메일")
    void should_ReturnBadRequest_When_InvalidEmail() throws Exception {
      // given - @Email 위반
      UserRegisterRequest request = new UserRegisterRequest(
          "not-an-email", NICKNAME, PASSWORD
      );

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).register(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 잘못된 입력(일력값 검증 실패) - 닉네임 길이 위반")
    void should_ReturnBadRequest_When_InvalidNickname() throws Exception {
      // given - @Size(min = 2, max = 20) 위반
      UserRegisterRequest request = new UserRegisterRequest(
          EMAIL, "A", PASSWORD
      );

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).register(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 잘못된 입력(일력값 검증 실패) - 비밀번호 패턴 위반")
    void should_ReturnBadRequest_When_InvalidPassword() throws Exception {
      // given - @Pattern 위반: 특수문자/숫자 누락
      UserRegisterRequest request = new UserRegisterRequest(
          EMAIL, NICKNAME, "onlyletters"
      );

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).register(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("실패(409) - 이메일 중복")
    void should_ReturnConflict_When_EmailAlreadyExists() throws Exception {
      // given
      UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, PASSWORD);
      // Service가 도메인 예외를 던지도록 설정
      given(userService.register(any(UserRegisterRequest.class)))
          .willThrow(new EmailAlreadyExistsException());

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
    }
  }

  @Nested
  @DisplayName("POST /api/users/login - 로그인")
  class Login {

    @Test
    @DisplayName("성공(200) - 로그인 성공")
    void should_ReturnOk_When_ValidCredentials() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserLoginRequest request = new UserLoginRequest(EMAIL, PASSWORD);
      UserDto response = new UserDto(userId, EMAIL, NICKNAME, OffsetDateTime.now());
      given(userService.login(any(UserLoginRequest.class))).willReturn(response);

      // when & then
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.email").value(EMAIL));

      verify(userService).login(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 잘못된 요청 (입력값 검증 실패) - 이메일이 비어 있음")
    void should_ReturnBadRequest_When_BlankEmail() throws Exception {
      // given - @NotBlank 위반
      UserLoginRequest request = new UserLoginRequest("", PASSWORD);

      // when & then
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).login(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("실패(401) - 로그인 실패 (이메일 또는 비밀번호 불일치) - 자격 증명 불일치")
    void should_ReturnUnauthorized_When_CredentialsMismatch() throws Exception {
      // given - 잘못된 비밀번호
      UserLoginRequest request = new UserLoginRequest(EMAIL, "WrongPassword1!");
      given(userService.login(any(UserLoginRequest.class)))
          .willThrow(new LoginFailedException());

      // when & then
      mockMvc.perform(post("/api/users/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(ErrorCode.LOGIN_FAILED.getCode()));
    }
  }

  @Nested
  @DisplayName("GET /api/users/{userId} - 사용자 정보 조회")
  class GetUser {

    @Test
    @DisplayName("성공(200) - 사용자 정보 조회 성공")
    void should_ReturnOk_When_UserExists() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserDto response = new UserDto(userId, EMAIL, NICKNAME, OffsetDateTime.now());
      given(userService.getUser(userId)).willReturn(response);

      // when & then
      mockMvc.perform(get("/api/users/{userId}", userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.nickname").value(NICKNAME));

      verify(userService).getUser(userId);
    }

    @Test
    @DisplayName("실패(404) - 사용자 정보 없음")
    void should_ReturnNotFound_When_UserNotExists() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      given(userService.getUser(userId)).willThrow(new UserNotFoundException(userId));

      // when & then
      mockMvc.perform(get("/api/users/{userId}", userId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }
  }

  @Nested
  @DisplayName("DELETE /api/users/{userId} - 사용자 논리 삭제")
  class DeleteUser {

    @Test
    @DisplayName("성공(204) - 사용자 삭제 성공")
    void should_ReturnNoContent_When_OwnerRequests() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willDoNothing().given(userService).deleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString()))
          .andExpect(status().isNoContent());

      verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("실패(403) - 사용자 삭제 권한 없음")
    void should_ReturnForbidden_When_NotOwner() throws Exception {
      // given
      UUID pathUserId = UUID.randomUUID();
      UUID requestUserId = UUID.randomUUID();

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", pathUserId)
              .header(AuthHeader.REQUEST_USER_ID, requestUserId.toString()))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_ACCESS_DENIED.getCode()));

      verify(userService, never()).deleteUser(any(UUID.class));
    }

    @Test
    @DisplayName("실패(404) - 사용자 정보 없음")
    void should_ReturnNotFound_When_UserNotExists() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willThrow(new UserNotFoundException(userId)).given(userService).deleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }
  }

  @Nested
  @DisplayName("PATCH /api/users/{userId} - 사용자 정보 수정")
  class UpdateUser {

    @Test
    @DisplayName("성공(200) - 사용자 정보 수정 성공")
    void should_ReturnOk_When_OwnerRequestsValidNickname() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      String newNickname = "변경된닉네임";
      UserUpdateRequest request = new UserUpdateRequest(newNickname);
      UserDto response = new UserDto(userId, EMAIL, newNickname, OffsetDateTime.now());
      given(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).willReturn(response);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))) // UserUpdateRequest를 JSON으로 직렬화
          // 응답 상태 코드가 200 OK인지 검증
          .andExpect(status().isOk())
          // 응답 JSON의 id 필드가 userId와 일치하는지 검증
          .andExpect(jsonPath("$.id").value(userId.toString()))
          // 응답 JSON의 nickname 필드가 변경된 닉네임과 일치하는지 검증
          .andExpect(jsonPath("$.nickname").value(newNickname));

      // updateUser()가 1번 호출되었는지 확인
      verify(userService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("실패(403) - 본인 아님: path userId와 헤더 userId 불일치")
    void should_ReturnForbidden_When_NotOwner() throws Exception {
      // given
      UUID pathUserId = UUID.randomUUID();
      UUID requestUserId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("변경된닉네임");

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", pathUserId)
              // 헤더에 다른 사용자의 ID를 넣음 -> 본인이 아님
              .header(AuthHeader.REQUEST_USER_ID, requestUserId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden());

      // Service가 호출되지 않았음을 검증
      verify(userService, never()).updateUser(any(UUID.class), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 요청자 헤더 누락")
    void should_ReturnBadRequest_When_HeaderMissing() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("변경된닉네임");

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              // 헤더를 넣지 않음 -> 누락
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          // ErrorCode가 MISSING_REQUEST_USER_ID 인지 검증 (GlobalExceptionHandler에서 제대로 잡히는지 확인)
          .andExpect(jsonPath("$.code").value(ErrorCode.MISSING_REQUEST_USER_ID.getCode()));

      verify(userService, never()).updateUser(any(UUID.class), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("실패(400) - 닉네임 길이 위반")
    void should_ReturnBadRequest_When_InvalidNickname() throws Exception {
      // given - @Size(min = 2, max = 20) 위반
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("A");

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updateUser(any(UUID.class), any(UserUpdateRequest.class));
    }
  }

  @Nested
  @DisplayName("DELETE /api/users/{userId}/hard - 사용자 물리 삭제")
  class PermanentDeleteUser {

    @Test
    @DisplayName("성공(204) - 사용자 삭제 성공")
    void should_ReturnNoContent_When_OwnerRequests() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willDoNothing().given(userService).permanentDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString()))
          .andExpect(status().isNoContent());

      verify(userService).permanentDeleteUser(userId);
    }

    @Test
    @DisplayName("실패(403) - 사용자 삭제 권한 없음")
    void should_ReturnForbidden_When_NotOwner() throws Exception {
      // given
      UUID pathUserId = UUID.randomUUID();
      UUID requestUserId = UUID.randomUUID();

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", pathUserId)
              .header(AuthHeader.REQUEST_USER_ID, requestUserId.toString()))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_ACCESS_DENIED.getCode()));

      verify(userService, never()).permanentDeleteUser(any(UUID.class));
    }

    @Test
    @DisplayName("실패(404) - 사용자 정보 없음 - 논리 삭제되지 않은 사용자")
    void should_ReturnNotFound_When_NotSoftDeleted() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willThrow(new UserNotSoftDeletedException(userId))
          .given(userService).permanentDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_SOFT_DELETED.getCode()));
    }

    @Test
    @DisplayName("실패(404) - 사용자 정보 없음 - 존재하지 않는 사용자")
    void should_ReturnNotFound_When_UserNotExists() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willThrow(new UserNotFoundException(userId))
          .given(userService).permanentDeleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}/hard", userId)
              .header(AuthHeader.REQUEST_USER_ID, userId.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }
  }
}
