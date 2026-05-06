package com.team01.deokhugam.dashboard.popularreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewSearchCondition;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import com.team01.deokhugam.dashboard.popularreview.mapper.PopularReviewMapper;
import com.team01.deokhugam.dashboard.popularreview.repository.PopularReviewRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.notification.repository.NotificationRepository;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class PopularReviewServiceImplTest {

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private PopularReviewMapper popularReviewMapper;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private PlatformTransactionManager transactionManager;

  @InjectMocks
  private PopularReviewServiceImpl popularReviewService;

  private UUID reviewId;
  private UUID userId;
  private OffsetDateTime start;
  private OffsetDateTime end;
  private LocalDate calculatedDate;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    start = time(21, 0);
    end = time(22, 0);
    calculatedDate = LocalDate.of(2026, 4, 21);
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 성공 - nextCursor / hasNext 값 검사")
  void getPopularReviews_success() {
    OffsetDateTime time1 = time(21, 10);
    OffsetDateTime time2 = time(21, 11);
    PopularReview pr1 = mock(PopularReview.class);
    PopularReview pr2 = mock(PopularReview.class);
    PopularReview pr3 = mock(PopularReview.class);

    given(pr2.getRank()).willReturn(2);
    given(pr2.getCreatedAt()).willReturn(time2);

    PopularReviewDto dto1 = createDto(1, time1);
    PopularReviewDto dto2 = createDto(2, time2);

    given(popularReviewRepository.findAllByCondition(any(PopularReviewSearchCondition.class)))
        .willReturn(List.of(pr1, pr2, pr3));
    given(popularReviewRepository.countByCondition(any(PopularReviewSearchCondition.class)))
        .willReturn(3L);
    given(popularReviewMapper.toDtoList(List.of(pr1, pr2)))
        .willReturn(List.of(dto1, dto2));

    CursorPageResponsePopularReviewDto result = popularReviewService.getPopularReviews(
        DashboardPeriod.DAILY,
        SortDirection.ASC,
        null,
        null,
        2
    );

    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo("2");
    assertThat(result.nextAfter()).isEqualTo(time2);
    assertThat(result.totalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 성공 - 다음 페이지가 없으면 nextCursor / nextAfter 는 null 이다")
  void getPopularReviews_success_withoutNextPage() {
    OffsetDateTime time1 = time(21, 10);
    OffsetDateTime time2 = time(21, 11);

    PopularReview pr1 = mock(PopularReview.class);
    PopularReview pr2 = mock(PopularReview.class);

    PopularReviewDto dto1 = createDto(1, time1);
    PopularReviewDto dto2 = createDto(2, time2);

    given(popularReviewRepository.findAllByCondition(any(PopularReviewSearchCondition.class)))
        .willReturn(List.of(pr1, pr2));
    given(popularReviewRepository.countByCondition(any(PopularReviewSearchCondition.class)))
        .willReturn(2L);
    given(popularReviewMapper.toDtoList(List.of(pr1, pr2)))
        .willReturn(List.of(dto1, dto2));

    CursorPageResponsePopularReviewDto result = popularReviewService.getPopularReviews(
        DashboardPeriod.DAILY,
        SortDirection.ASC,
        null,
        null,
        2
    );

    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextAfter()).isNull();
    assertThat(result.totalElements()).isEqualTo(2);
  }

  @Test
  @DisplayName("인기 리뷰 집계 - 10위권 리뷰는 알림을 생성한다")
  void calculatePopularReviews_createsNotification_whenRankInTop10() {
    Review review = mockReviewWithUser();
    String content = DashboardPeriod.DAILY + " 인기 리뷰 10위 안에 진입했습니다.";

    givenPopularReviewScoreExists();
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));
    given(notificationRepository.existsByReviewIdAndUserIdAndContent(
        reviewId,
        userId,
        content
    )).willReturn(false);
    given(transactionManager.getTransaction(any()))
        .willReturn(new SimpleTransactionStatus());

    popularReviewService.calculatePopularReviews(
        DashboardPeriod.DAILY,
        calculatedDate,
        start,
        end
    );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification.getReview()).isEqualTo(review);
    assertThat(savedNotification.getUser().getId()).isEqualTo(userId);
    assertThat(savedNotification.getContent()).isEqualTo(content);
  }

  @Test
  @DisplayName("인기 리뷰 집계 - 이미 10위권 알림이 있으면 중복 생성하지 않는다")
  void calculatePopularReviews_doesNotCreateNotification_whenAlreadyExists() {
    Review review = mockReviewWithUser();
    String content = DashboardPeriod.DAILY + " 인기 리뷰 10위 안에 진입했습니다.";

    givenPopularReviewScoreExists();
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));
    given(notificationRepository.existsByReviewIdAndUserIdAndContent(
        reviewId,
        userId,
        content
    )).willReturn(true);
    given(transactionManager.getTransaction(any()))
        .willReturn(new SimpleTransactionStatus());

    popularReviewService.calculatePopularReviews(
        DashboardPeriod.DAILY,
        calculatedDate,
        start,
        end
    );

    verify(notificationRepository, never()).save(any(Notification.class));
  }

  private void givenPopularReviewScoreExists() {
    given(popularReviewRepository.findPopularReviewLikeScoreRows(start, end))
        .willReturn(List.of(new PopularReviewScoreRow(reviewId, 10L, 0L)));
    given(popularReviewRepository.findPopularReviewCommentScoreRows(start, end))
        .willReturn(List.of());
  }

  private Review mockReviewWithUser() {
    Review review = mock(Review.class);
    User user = mock(User.class);

    given(review.getId()).willReturn(reviewId);
    given(review.getUser()).willReturn(user);
    given(user.getId()).willReturn(userId);

    return review;
  }

  private PopularReviewDto createDto(int rank, OffsetDateTime createdAt) {
    return new PopularReviewDto(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "책" + rank,
        "thumbnail-" + rank + ".jpg",
        UUID.randomUUID(),
        "유저" + rank,
        "리뷰 내용" + rank,
        4.5,
        DashboardPeriod.DAILY,
        createdAt,
        rank,
        10.0,
        10,
        0
    );
  }

  @Test
  @DisplayName("인기 리뷰 집계 - 10위 밖 리뷰는 알림을 생성하지 않는다")
  void calculatePopularReviews_doesNotCreateNotification_whenRankOutOfTop10() {
    List<UUID> reviewIds = new ArrayList<>();
    List<PopularReviewScoreRow> scoreRows = new ArrayList<>();
    List<Review> reviews = new ArrayList<>();

    for (int i = 1; i <= 11; i++) {
      UUID id = UUID.randomUUID();
      Review review = mock(Review.class);
      User user = mock(User.class);

      given(review.getId()).willReturn(id);

      if (i <= 10) {
        given(review.getUser()).willReturn(user);
        given(user.getId()).willReturn(UUID.randomUUID());
      }

      reviewIds.add(id);
      reviews.add(review);

      scoreRows.add(new PopularReviewScoreRow(
          id,
          12L - i,
          0L
      ));
    }

    given(popularReviewRepository.findPopularReviewLikeScoreRows(start, end))
        .willReturn(scoreRows);
    given(popularReviewRepository.findPopularReviewCommentScoreRows(start, end))
        .willReturn(List.of());
    given(reviewRepository.findAllById(reviewIds))
        .willReturn(reviews);
    given(transactionManager.getTransaction(any()))
        .willReturn(new SimpleTransactionStatus());

    popularReviewService.calculatePopularReviews(
        DashboardPeriod.DAILY,
        calculatedDate,
        start,
        end
    );

    verify(notificationRepository, times(10)).save(any(Notification.class));
  }

  private OffsetDateTime time(int day, int hour) {
    return OffsetDateTime.of(2026, 4, day, hour, 0, 0, 0, ZoneOffset.UTC);
  }
}
