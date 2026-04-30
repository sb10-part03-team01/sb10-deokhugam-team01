package com.team01.deokhugam.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.dto.NotificationUpdateRequest;
import com.team01.deokhugam.notification.service.NotificationService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private NotificationService notificationService;

  private UUID userId;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("confirm 메서드 테스트")
  class confirm {

    @Test
    @DisplayName("confirm 성공 테스트")
    void confirmSuccess() throws Exception {
      //given
      UUID notificationId = UUID.randomUUID();
      NotificationUpdateRequest request = new NotificationUpdateRequest(true);
      NotificationDto testDto = new NotificationDto(notificationId, userId, null, null, null, true,
          null, null);
      given(notificationService.confirm(any(UUID.class), any(UUID.class))).willReturn(testDto);

      //when,then
      mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
              .header("Deokhugam-Request-User-ID", userId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(notificationId.toString()))
          .andExpect(jsonPath("$.confirmed").value(true));
    }
  }

  @Nested
  @DisplayName("findAll 메서드 테스트")
  class findAll {

    @Test
    @DisplayName("findAll 성공 테스트")
    void findAllSuccess() throws Exception {
      //given
      NotificationDto dto = new NotificationDto(UUID.randomUUID(), userId, null, null, "알림 테스트",
          false, OffsetDateTime.now(), null);
      List<NotificationDto> content = List.of(dto);
      CursorPageResponse<NotificationDto> response = new CursorPageResponse<>(
          content,
          "next-cursor-uuid",
          null,
          content.size(),
          1L,
          true
      );
      given(notificationService.findAll(eq(userId), any(CursorPageRequest.class),
          any(SortDirection.class)))
          .willReturn(response);

      //when, then
      mockMvc.perform(get("/api/notifications")
              .header("Deokhugam-Request-User-ID", userId.toString())
              .queryParam("direction", "DESC")
              .queryParam("limit", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].message").value("알림 테스트"))
          .andExpect(jsonPath("$.nextCursor").value("next-cursor-uuid"));
    }
  }

}
