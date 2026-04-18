package com.team01.deokhugam.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.global.constant.AuthHeader;
import com.team01.deokhugam.user.dto.UserDto;
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

  // MockMvc: Spring MVC 테스트를 위한 도구
  @Autowired
  private MockMvc mockMvc;

  // ObjectMapper: Jackson의 JSON 직렬화/역직렬화 도구
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Nested
  @DisplayName("PATCH /api/users/{userId} - 사용자 정보 수정")
  class UpdateUser {

    @Test
    @DisplayName("성공 - 본인 요청, 유효한 닉네임")
    void updateUser_Success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      String newNickname = "변경된닉네임";
      UserUpdateRequest request = new UserUpdateRequest(newNickname);
      UserDto response = new UserDto(userId, "test@email.com", newNickname, OffsetDateTime.now());
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
    void updateUser_Fail_AccessDenied() throws Exception {
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
    void updateUser_Fail_MissingHeader() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("변경된닉네임");

      // when & then
      mockMvc.perform(patch("/api/users/{userId}", userId)
              // 헤더를 넣지 않음 -> 누락
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updateUser(any(UUID.class), any(UserUpdateRequest.class));
    }
  }
}
