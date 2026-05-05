package com.team01.deokhugam.dashboard.poweruser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import com.team01.deokhugam.dashboard.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.user.dto.PowerUserDto;
import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PowerUserServiceTest {

  @InjectMocks
  private PowerUserServiceImpl powerUserService;

  @Mock
  private PowerUserRepository powerUserRepository;

  private User mockUser;
  private PowerUser mockPowerUser;

  @BeforeEach
  void setUp() {
    mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(UUID.randomUUID());
    given(mockUser.getNickname()).willReturn("testUser");

    mockPowerUser = mock(PowerUser.class);
    given(mockPowerUser.getUser()).willReturn(mockUser);
    given(mockPowerUser.getPeriod()).willReturn(DashboardPeriod.DAILY);
    given(mockPowerUser.getCreatedAt()).willReturn(OffsetDateTime.now());
    given(mockPowerUser.getRank()).willReturn(1L);
    given(mockPowerUser.getScore()).willReturn(100.0);
    given(mockPowerUser.getReviewScoreSum()).willReturn(50.0);
    given(mockPowerUser.getLikeCount()).willReturn(10L);
    given(mockPowerUser.getCommentCount()).willReturn(5L);
  }

  @Nested
  @DisplayName("getRanking")
  class getRanking {

    @Test
    @DisplayName("after == null, cursor == null, asc 조회")
    void findByPeriodOrderByRankAscSuccess() {
      given(powerUserRepository.findByPeriodOrderByRankAsc(any(), any()))
          .willReturn(List.of(mockPowerUser));
      given(powerUserRepository.countByPeriod(any()))
          .willReturn(1L);

      CursorPageResponse<PowerUserDto> response =
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.ASC, null, null, 10);

      assertThat(response.content()).hasSize(1);
      verify(powerUserRepository).findByPeriodOrderByRankAsc(eq(DashboardPeriod.DAILY), any(PageRequest.class));
    }

    @Test
    @DisplayName("after == null, cursor == null , desc 조회")
    void findByPeriodOrderByRankDescSuccess() {
      //given
      given(powerUserRepository.findByPeriodOrderByRankDesc(any(), any()))
          .willReturn(List.of(mockPowerUser));

      given(powerUserRepository.countByPeriod(any()))
          .willReturn(1L);

      //when

      CursorPageResponse<PowerUserDto> response =
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.DESC, null, null, 10);
      //then
      assertThat(response.content()).hasSize(1);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.totalElements()).isEqualTo(1L);
      assertThat(response.nextCursor()).isNull();
      assertThat(response.nextAfter()).isNull();

      verify(powerUserRepository).findByPeriodOrderByRankDesc(eq(DashboardPeriod.DAILY), any(
          PageRequest.class));

    }

    @Test
    @DisplayName("after == null, cursor != null 예외 케이스")
    void afterNullCursorNotNull_throwsException() {
      //when, then
      assertThatThrownBy(() ->
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.DESC, "1", null, 10)
      )
          .isInstanceOf(DeokhugamException.class)
          .satisfies(e -> {
            DeokhugamException ex = (DeokhugamException) e;

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
          });
    }

    @Test
    @DisplayName("after != null, cursor == null 예외 케이스")
    void afterNotNullCursorNull_throwsException() {
      //when, then
      assertThatThrownBy(() ->
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.DESC, null,
              OffsetDateTime.now(), 10))
          .isInstanceOf(DeokhugamException.class)
          .satisfies(e -> {
            DeokhugamException ex = (DeokhugamException) e;

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
          });
    }

    @Test
    @DisplayName("cursor이 숫자가 아닌경우")
    void cursorNotNumber_throwsException() {
      //when, then
      assertThatThrownBy(() ->
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.DESC, "test",
              OffsetDateTime.now(), 10))
          .isInstanceOf(DeokhugamException.class)
          .satisfies(e -> {
            DeokhugamException ex = (DeokhugamException) e;

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);

          });
    }

    @Test
    @DisplayName("after != null, cursor가 정상인 경우")
    void afterNotNullValidCursor_success() {
      //given
      OffsetDateTime after = OffsetDateTime.now();

      given(powerUserRepository.findNextPageDesc(any(), any(), any(), any()))
          .willReturn(List.of(mockPowerUser));
      given(powerUserRepository.countByPeriod(any()))
          .willReturn(1L);

      //when
      CursorPageResponse<PowerUserDto> response =
          powerUserService.getRanking(DashboardPeriod.DAILY, SortDirection.DESC, "1", after, 10);

      //then
      assertThat(response.content()).hasSize(1);
      verify(powerUserRepository).findNextPageDesc(eq(DashboardPeriod.DAILY), eq(1L), eq(after),
          any(PageRequest.class));
    }


  }

}
