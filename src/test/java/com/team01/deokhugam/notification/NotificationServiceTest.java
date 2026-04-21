package com.team01.deokhugam.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.team01.deokhugam.global.exception.notification.NotificationException;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.notification.dto.NotificationCreateRequest;
import com.team01.deokhugam.notification.dto.NotificationDto;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.repository.NotificationRepository;
import com.team01.deokhugam.notification.service.NotificationServiceImpl;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @InjectMocks
  private NotificationServiceImpl notificationService;
  @Mock
  private NotificationRepository notificationRepository;

  @Nested
  @DisplayName("알림 생성 테스트")
  class NotificationCreateTest {

    private User mockReviewOwner;

    @BeforeEach
    void setup() {
      mockReviewOwner = mock(User.class);
      given(mockReviewOwner.getId()).willReturn(UUID.randomUUID());
    }

    @Test
    @DisplayName("알림 생성 테스트 성공")
    void NotificationCreateSuccess() {
      //given
      User actor = mock(User.class);
      given(actor.getId()).willReturn(UUID.randomUUID());

      Review review = mock(Review.class);
      given(review.getUser()).willReturn(mockReviewOwner);

      NotificationCreateRequest request = new NotificationCreateRequest(review, actor,
          "테스트 내용");
      //when
      notificationService.create(request);
      //then
      then(notificationRepository).should().save(any(Notification.class));

    }

    @Test
    @DisplayName("알림 생성 테스트 실패")
    void NotificationCreateFail() {
      //given
      User actor = mockReviewOwner;

      Review review = mock(Review.class);
      given(review.getUser()).willReturn(mockReviewOwner);

      NotificationCreateRequest request = new NotificationCreateRequest(review, actor,
          "테스트 내용");
      //when
      notificationService.create(request);

      //then
      then(notificationRepository).should(never()).save(any(Notification.class));

    }

  }

  @Nested
  @DisplayName("알림 상태 수정 테스트")
  class NotificationConfirmTest {

    @Test
    @DisplayName("알림 상태 수정 테스트 성공")
    void NotificationConfirmSuccess() {
      //given
      Notification notification = mock(Notification.class);
      given(notification.isRead()).willReturn(false);
      //when
      notificationService.confirm(notification);
      //then
      then(notification).should().markAsRead();

    }

    @Test
    @DisplayName("알림 상태 수정 테스트 실패")
    void NotificationConfirmFail() {
      //given
      Notification notification = mock(Notification.class);
      given(notification.isRead()).willReturn(true);
      //when
      notificationService.confirm(notification);
      //then
      then(notification).should(never()).markAsRead();

    }
  }

  @Nested
  @DisplayName("알림 상태 전체 수정 테스트")
  class NotificationConfirmAllTest {

    private User mockUser;

    @BeforeEach
    void setup() {
      mockUser = mock(User.class);
      given(mockUser.getId()).willReturn(UUID.randomUUID());
    }

    @Test
    @DisplayName("알림 상태 전체 수정 테스트 성공")
    void NotificationConfirmAllSuccess() {
      //given
      Notification notification1 = mock(Notification.class);
      Notification notification2 = mock(Notification.class);
      given(notificationRepository.findAllByUserIdAndIsReadFalse(mockUser.getId()))
          .willReturn(List.of(notification1, notification2));
      //when
      notificationService.confirmAll(mockUser);
      //then
      then(notification1).should().markAsRead();
      then(notification2).should().markAsRead();
    }

    @Test
    @DisplayName("알림 상태 수정 테스트 - 리스트가 비었을 경우")
    void NotificationConfirmEmptyList() {
      //given
      given(notificationRepository.findAllByUserIdAndIsReadFalse(mockUser.getId()))
          .willReturn(List.of());
      //when
      notificationService.confirmAll(mockUser);
      //then
      then(notificationRepository).should(never()).saveAll(any());
    }
  }

  @Nested
  @DisplayName("알림 삭제 테스트")
  class NotificationCleanupRead {

    @Test
    @DisplayName("알림 삭제 테스트 성공")
    void NotificationCleanupReadSuccess() {
      //given
      //when
      notificationService.cleanupReadNotifications();
      //then
      then(notificationRepository).should()
          .deleteAllByIsReadTrueAndUpdatedAtBefore(any(OffsetDateTime.class));
    }
  }

  @Nested
  @DisplayName("알림 목록 조회 테스트")
  class NotificationFindAllTest {

    private User mockUser;
    private Review mockReview;
    private CursorPageRequest mockCursorPageRequest;

    @BeforeEach
    void setup() {
      mockUser = mock(User.class);
      given(mockUser.getId()).willReturn(UUID.randomUUID());
      mockReview = mock(Review.class);
    }

    @Test
    @DisplayName("알림 목록 조회 테스트 성공 - 첫 페이지")
    void NotificationFindAllSuccess() {
      //given
      mockCursorPageRequest = new CursorPageRequest(null, null, 50);
      Notification notification = mock(Notification.class);
      given(notification.getId()).willReturn(UUID.randomUUID());
      given(notification.getUser()).willReturn(mockUser);
      given(notification.getReview()).willReturn(mockReview);
      given(notification.getContent()).willReturn("알림 목록 조회 테스트");
      given(notification.isRead()).willReturn(false);
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      given(notification.getCreatedAt()).willReturn(fixedTime);
      given(notification.getUpdatedAt()).willReturn(fixedTime);
      given(mockReview.getId()).willReturn(UUID.randomUUID());
      given(mockReview.getContent()).willReturn("테스트내용");

      given(notificationRepository.findByUserIdOrderByCreatedAtDesc(any(UUID.class), any(
          PageRequest.class)))
          .willReturn(List.of(notification));
      given(notificationRepository.countByUserId(any(UUID.class))).willReturn(1L);
      //when
      CursorPageResponse<NotificationDto> result = notificationService.findAll(mockUser.getId(),
          mockCursorPageRequest);
      //then
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isFalse();

    }

    @Test
    @DisplayName("알림 목록 조회 테스트 성공 - 다음 페이지")
    void NotificationFindAllSuccessNext() {
      //given
      Notification notification = mock(Notification.class);
      given(notification.getId()).willReturn(UUID.randomUUID());
      given(notification.getUser()).willReturn(mockUser);
      given(notification.getReview()).willReturn(mockReview);
      given(notification.getContent()).willReturn("알림 목록 조회 테스트");
      given(notification.isRead()).willReturn(false);
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      mockCursorPageRequest = new CursorPageRequest(UUID.randomUUID().toString(), fixedTime, 50);
      given(notification.getCreatedAt()).willReturn(fixedTime);
      given(notification.getUpdatedAt()).willReturn(fixedTime);
      given(mockReview.getId()).willReturn(UUID.randomUUID());
      given(mockReview.getContent()).willReturn("테스트내용");

      given(notificationRepository.findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          any(UUID.class), any(OffsetDateTime.class), any(UUID.class), any(PageRequest.class)))
          .willReturn(List.of(notification));
      given(notificationRepository.countByUserId(any(UUID.class))).willReturn(1L);

      //when
      CursorPageResponse<NotificationDto> result = notificationService.findAll(mockUser.getId(),
          mockCursorPageRequest);

      //then
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("알림 목록 조회 테스트 실패 - after가 null이 아닌데 cursor가 null")
    void NotificationFindAllTestFail1() {
      //given
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      mockCursorPageRequest = new CursorPageRequest(null, fixedTime, 50);
      //when,then
      assertThatThrownBy(() -> notificationService.findAll(mockUser.getId(), mockCursorPageRequest))
          .isInstanceOf(NotificationException.class);

    }

    @Test
    @DisplayName("알림 목록 조회 테스트 실패 - cursor이 잘못된 UUID 포맷인경우")
    void NotificationFindAllTestFail2() {
      //given
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      mockCursorPageRequest = new CursorPageRequest("invalid-uuid", fixedTime, 50);
      //when,then
      assertThatThrownBy(() -> notificationService.findAll(mockUser.getId(), mockCursorPageRequest))
          .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("알림 목록 조회 테스트 실패 - after이 null이고 cursor이 null이 아닌경우")
    void NotificationFindAllTestFail3(){
      //given
      mockCursorPageRequest = new CursorPageRequest(UUID.randomUUID().toString(), null, 50);
      //when,then
      assertThatThrownBy(() -> notificationService.findAll(mockUser.getId(), mockCursorPageRequest))
          .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("알림 목록 조회 테스트 성공 - hasNext = true 인 경우")
    void NotificationFindAllTestHasNext(){
      //given
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      mockCursorPageRequest = new CursorPageRequest(null, null, 2);
      Notification notification1 = mock(Notification.class);
      Notification notification2 = mock(Notification.class);
      Notification notification3 = mock(Notification.class);

      List<Notification> testResult = List.of(notification1, notification2, notification3);

      given(mockReview.getId()).willReturn(UUID.randomUUID());
      given(mockReview.getContent()).willReturn("테스트내용");

      for (Notification n : testResult) {
        given(n.getId()).willReturn(UUID.randomUUID());
        given(n.getUser()).willReturn(mockUser);
        given(n.getReview()).willReturn(mockReview);
        given(n.getContent()).willReturn("알림 내용");
        given(n.isRead()).willReturn(false);
        given(n.getCreatedAt()).willReturn(fixedTime);
        given(n.getUpdatedAt()).willReturn(fixedTime);
      }

      given(notificationRepository.findByUserIdOrderByCreatedAtDesc(any(UUID.class), any(
          PageRequest.class)))
          .willReturn(testResult);

      given(notificationRepository.countByUserId(any(UUID.class))).willReturn(3L);

      //when
      CursorPageResponse<NotificationDto> result = notificationService.findAll(mockUser.getId(),
          mockCursorPageRequest);
      //then
      assertThat(result.content()).hasSize(2);
      assertThat(result.hasNext()).isTrue();


    }

    @Test
    @DisplayName("알림 조회 테스트 성공 - limit가 null인 경우")
    void NotificationFindAllLimitNull(){
      //given
      mockCursorPageRequest = new CursorPageRequest(null, null, null);
      Notification notification = mock(Notification.class);
      given(notification.getId()).willReturn(UUID.randomUUID());
      given(notification.getUser()).willReturn(mockUser);
      given(notification.getReview()).willReturn(mockReview);
      given(notification.getContent()).willReturn("알림 목록 조회 테스트");
      given(notification.isRead()).willReturn(false);
      OffsetDateTime fixedTime = OffsetDateTime.of(2026, 4, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      given(notification.getCreatedAt()).willReturn(fixedTime);
      given(notification.getUpdatedAt()).willReturn(fixedTime);
      given(mockReview.getId()).willReturn(UUID.randomUUID());
      given(mockReview.getContent()).willReturn("테스트내용");

      given(notificationRepository.findByUserIdOrderByCreatedAtDesc(any(UUID.class), any(
          PageRequest.class)))
          .willReturn(List.of(notification));
      given(notificationRepository.countByUserId(any(UUID.class))).willReturn(1L);
      //when
      CursorPageResponse<NotificationDto> result = notificationService.findAll(mockUser.getId(),
          mockCursorPageRequest);
      //then
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isFalse();

    }


  }

}
