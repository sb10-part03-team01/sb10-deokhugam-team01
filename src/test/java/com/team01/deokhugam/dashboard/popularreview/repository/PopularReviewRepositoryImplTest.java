package com.team01.deokhugam.dashboard.popularreview.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.config.QuerydslTestConfig;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewSearchCondition;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@Import(QuerydslTestConfig.class)
@DataJpaTest
@TestPropertySource(
    properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class PopularReviewRepositoryImplTest {

  @Autowired
  private PopularReviewRepositoryImpl popularReviewRepository;

  @Autowired
  private EntityManager em;

  private LocalDate calculatedDate;
  private LocalDate otherDate;
  private OffsetDateTime time1;
  private OffsetDateTime time2;
  private OffsetDateTime time3;

  @BeforeEach
  void setUp() {
    calculatedDate = LocalDate.of(2026, 4, 21);
    otherDate = LocalDate.of(2026, 4, 20);

    time1 = time(10);
    time2 = time(12);
    time3 = time(14);
  }

  @Test
  @DisplayName("첫 페이지를 rank ASC 기준으로 limit + 1개 조회한다")
  void find_all_by_condition_first_page_asc_returns_limit_plus_one() {
    Book book = persistBook("책1", "저자1", "isbn-1");

    Review review1 = persistReview(book, persistUser("user1@test.com", "user1"), "리뷰1");
    Review review2 = persistReview(book, persistUser("user2@test.com", "user2"), "리뷰2");
    Review review3 = persistReview(book, persistUser("user3@test.com", "user3"), "리뷰3");

    persistPopularReview(review1, DashboardPeriod.DAILY, calculatedDate, 1, 10.0, 10, 0, time1);
    persistPopularReview(review2, DashboardPeriod.DAILY, calculatedDate, 2, 9.0, 8, 1, time2);
    persistPopularReview(review3, DashboardPeriod.DAILY, calculatedDate, 3, 8.0, 6, 2, time3);

    em.flush();
    em.clear();

    PopularReviewSearchCondition condition = condition(
        SortDirection.ASC,
        null,
        null,
        2
    );

    List<PopularReview> result = popularReviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(3);
    assertThat(result)
        .extracting(PopularReview::getRank)
        .containsExactly(1, 2, 3);
  }

  @Test
  @DisplayName("period와 calculatedDate가 일치하는 데이터만 조회한다")
  void find_all_by_condition_filters_by_period_and_calculated_date() {
    Book book = persistBook("책4", "저자4", "isbn-4");

    Review dailyReview = persistReview(
        book,
        persistUser("user10@test.com", "user10"),
        "일간 리뷰"
    );
    Review weeklyReview = persistReview(
        book,
        persistUser("user11@test.com", "user11"),
        "주간 리뷰"
    );
    Review oldReview = persistReview(
        book,
        persistUser("user12@test.com", "user12"),
        "이전 리뷰"
    );

    persistPopularReview(dailyReview, DashboardPeriod.DAILY, calculatedDate, 1, 10.0, 10, 0, time1);
    persistPopularReview(weeklyReview, DashboardPeriod.WEEKLY, calculatedDate, 1, 9.0, 8, 1, time2);
    persistPopularReview(oldReview, DashboardPeriod.DAILY, otherDate, 1, 8.0, 6, 2, time3);

    em.flush();
    em.clear();

    PopularReviewSearchCondition condition = condition(
        SortDirection.ASC,
        null,
        null,
        10
    );

    List<PopularReview> result = popularReviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPeriod()).isEqualTo(DashboardPeriod.DAILY);
    assertThat(result.get(0).getCalculatedDate()).isEqualTo(calculatedDate);
    assertThat(result.get(0).getReview().getContent()).isEqualTo("일간 리뷰");
  }

  @Test
  @DisplayName("period와 calculatedDate가 일치하는 데이터 개수만 반환한다")
  void count_by_condition_filters_by_period_and_calculated_date() {
    Book book = persistBook("책5", "저자5", "isbn-5");

    Review review1 = persistReview(book, persistUser("user13@test.com", "user13"), "리뷰1");
    Review review2 = persistReview(book, persistUser("user14@test.com", "user14"), "리뷰2");
    Review weeklyReview = persistReview(book, persistUser("user15@test.com", "user15"), "주간 리뷰");
    Review oldReview = persistReview(book, persistUser("user16@test.com", "user16"), "이전 리뷰");

    persistPopularReview(review1, DashboardPeriod.DAILY, calculatedDate, 1, 10.0, 10, 0, time1);
    persistPopularReview(review2, DashboardPeriod.DAILY, calculatedDate, 2, 9.0, 8, 1, time2);
    persistPopularReview(weeklyReview, DashboardPeriod.WEEKLY, calculatedDate, 1, 8.0, 6, 2, time3);
    persistPopularReview(oldReview, DashboardPeriod.DAILY, otherDate, 1, 7.0, 4, 3, time3);

    em.flush();
    em.clear();

    PopularReviewSearchCondition condition = condition(
        SortDirection.ASC,
        null,
        null,
        10
    );

    long count = popularReviewRepository.countByCondition(condition);

    assertThat(count).isEqualTo(2L);
  }

  @Test
  @DisplayName("cursor만 있고 after가 없으면 INVALID_CURSOR_FORMAT 예외가 발생한다")
  void find_all_by_condition_fail_when_cursor_without_after() {
    PopularReviewSearchCondition condition = condition(
        SortDirection.DESC,
        "2",
        null,
        10
    );

    assertThatThrownBy(() -> popularReviewRepository.findAllByCondition(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(exception -> {
          DeokhugamException e = (DeokhugamException) exception;
          assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
        });
  }

  @Test
  @DisplayName("after만 있고 cursor가 없으면 INVALID_CURSOR_FORMAT 예외가 발생한다")
  void find_all_by_condition_fail_when_after_without_cursor() {
    PopularReviewSearchCondition condition = condition(
        SortDirection.DESC,
        null,
        time1,
        10
    );

    assertThatThrownBy(() -> popularReviewRepository.findAllByCondition(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(exception -> {
          DeokhugamException e = (DeokhugamException) exception;
          assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
        });
  }

  @Test
  @DisplayName("cursor가 숫자가 아니면 INVALID_CURSOR_FORMAT 예외가 발생한다")
  void find_all_by_condition_fail_when_cursor_is_not_number() {
    PopularReviewSearchCondition condition = condition(
        SortDirection.DESC,
        "abc",
        time1,
        10
    );

    assertThatThrownBy(() -> popularReviewRepository.findAllByCondition(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(exception -> {
          DeokhugamException e = (DeokhugamException) exception;
          assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
        });
  }

  private PopularReviewSearchCondition condition(
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit
  ) {
    return new PopularReviewSearchCondition(
        DashboardPeriod.DAILY,
        direction,
        cursor,
        after,
        limit,
        calculatedDate
    );
  }

  private User persistUser(String email, String nickname) {
    User user = new User(email, nickname, "1234");

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);

    em.persist(user);
    return user;
  }

  private Book persistBook(String title, String author, String isbn) {
    Book book = Book.builder()
        .title(title)
        .author(author)
        .description("설명")
        .publisher("출판사")
        .publishedDate(LocalDate.of(2026, 4, 1))
        .isbn(isbn)
        .thumbnailUrl("thumbnail.jpg")
        .build();

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ReflectionTestUtils.setField(book, "createdAt", now);
    ReflectionTestUtils.setField(book, "updatedAt", now);

    em.persist(book);
    return book;
  }

  private Review persistReview(Book book, User user, String content) {
    Review review = new Review(book, user, content, 4.0);

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ReflectionTestUtils.setField(review, "createdAt", now);
    ReflectionTestUtils.setField(review, "updatedAt", now);

    em.persist(review);
    return review;
  }

  private PopularReview persistPopularReview(
      Review review,
      DashboardPeriod period,
      LocalDate calculatedDate,
      int rank,
      double score,
      int likeCount,
      int commentCount,
      OffsetDateTime createdAt
  ) {
    PopularReview popularReview = new PopularReview(
        review,
        period,
        calculatedDate,
        rank,
        score,
        likeCount,
        commentCount
    );

    ReflectionTestUtils.setField(popularReview, "createdAt", createdAt);

    em.persist(popularReview);
    return popularReview;
  }

  private OffsetDateTime time(int hour) {
    return OffsetDateTime.of(2026, 4, 21, hour, 0, 0, 0, ZoneOffset.UTC);
  }
}
