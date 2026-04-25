package com.team01.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.global.config.JpaConfig;
import com.team01.deokhugam.global.config.QueryDslConfig;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import({ReviewRepositoryImpl.class, QueryDslConfig.class, JpaConfig.class})
@ActiveProfiles("test")
class ReviewRepositoryImplTest {

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private TestEntityManager em;

  private User user1;
  private User user2;
  private Book book1;
  private Book book2;

  private Review review1;
  private Review review2;
  private Review review3;
  private Review review4;

  @BeforeEach
  void setUp() {
    user1 = createUser("user1@test.com", "유저1", "password");
    user2 = createUser("user2@test.com", "유저2", "password");

    book1 = createBook("도서1", "isbn-1111");
    book2 = createBook("도서2", "isbn-2222");

    review1 = createReview(user1, book1, "리뷰 테스트 1", 3.5,
        OffsetDateTime.parse("2026-04-20T10:00:00+09:00"));
    review2 = createReview(user1, book2, "리뷰 테스트 2", 5.0,
        OffsetDateTime.parse("2026-04-20T11:00:00+09:00"));
    review3 = createReview(user2, book1, "리뷰 테스트 3", 4.0,
        OffsetDateTime.parse("2026-04-20T12:00:00+09:00"));
    review4 = createReview(user2, book2, "리뷰 테스트 4", 2.0,
        OffsetDateTime.parse("2026-04-20T13:00:00+09:00"));

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("리뷰 목록 조회 - createdAt 기준 내림차순 정렬 성공")
  void findAllByCondition_orderByCreatedAtDesc_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    List<Review> result = reviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(4);
    assertThat(result.get(0).getContent()).isEqualTo("리뷰 테스트 4");
    assertThat(result.get(1).getContent()).isEqualTo("리뷰 테스트 3");
    assertThat(result.get(2).getContent()).isEqualTo("리뷰 테스트 2");
    assertThat(result.get(3).getContent()).isEqualTo("리뷰 테스트 1");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - rating 기준 내림차순 정렬 성공")
  void findAllByCondition_orderByRatingDesc_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        null,
        null,
        "rating",
        SortDirection.DESC,
        null,
        null,
        10
    );

    List<Review> result = reviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(4);
    assertThat(result.get(0).getRating()).isEqualTo(5.0);
    assertThat(result.get(1).getRating()).isEqualTo(4.0);
    assertThat(result.get(2).getRating()).isEqualTo(3.5);
    assertThat(result.get(3).getRating()).isEqualTo(2.0);
  }

  @Test
  @DisplayName("리뷰 목록 조회 - userId 조건으로 필터링 성공")
  void findAllByCondition_filterByUserId_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        user1.getId(),
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    List<Review> result = reviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(Review::getContent)
        .containsExactly("리뷰 테스트 2", "리뷰 테스트 1");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - bookId 조건으로 필터링 성공")
  void findAllByCondition_filterByBookId_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        book1.getId(),
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    List<Review> result = reviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(Review::getContent)
        .containsExactly("리뷰 테스트 3", "리뷰 테스트 1");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - keyword 조건으로 필터링 성공")
  void findAllByCondition_filterByKeyword_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        null,
        "리뷰 테스트",
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    List<Review> result = reviewRepository.findAllByCondition(condition);

    assertThat(result).hasSize(4);
    assertThat(result)
        .extracting(Review::getContent)
        .containsExactly("리뷰 테스트 4", "리뷰 테스트 3", "리뷰 테스트 2", "리뷰 테스트 1");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - after와 cursor로 다음 페이지 조회 성공")
  void findAllByCondition_cursorPaging_success() {
    ReviewSearchCondition firstCondition = new ReviewSearchCondition(
        null,
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        2
    );

    List<Review> firstPage = reviewRepository.findAllByCondition(firstCondition);

    Review lastReviewOfFirstPage = firstPage.get(1);

    ReviewSearchCondition secondCondition = new ReviewSearchCondition(
        null,
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        lastReviewOfFirstPage.getId().toString(),
        lastReviewOfFirstPage.getCreatedAt(),
        2
    );

    List<Review> secondPage = reviewRepository.findAllByCondition(secondCondition);

    assertThat(firstPage).hasSize(3);
    assertThat(secondPage).hasSize(2);
    assertThat(secondPage)
        .extracting(Review::getContent)
        .containsExactly("리뷰 테스트 2", "리뷰 테스트 1");
  }

  @Test
  @DisplayName("리뷰 목록 조회 - after만 전달되면 예외 발생")
  void findAllByCondition_fail_whenAfterOnly() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        OffsetDateTime.now(),
        10
    );

    assertThatThrownBy(() -> reviewRepository.findAllByCondition(condition))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
  }

  @Test
  @DisplayName("리뷰 목록 조회 - cursor만 전달되면 예외 발생")
  void findAllByCondition_fail_whenCursorOnly() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        null,
        null,
        "createdAt",
        SortDirection.DESC,
        review1.getId().toString(),
        null,
        10
    );

    assertThatThrownBy(() -> reviewRepository.findAllByCondition(condition))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
  }

  @Test
  @DisplayName("리뷰 개수 조회 - 조건에 맞는 개수 반환 성공")
  void countByCondition_success() {
    ReviewSearchCondition condition = new ReviewSearchCondition(
        null,
        book1.getId(),
        null,
        "createdAt",
        SortDirection.DESC,
        null,
        null,
        10
    );

    long count = reviewRepository.countByCondition(condition);

    assertThat(count).isEqualTo(2);
  }

  private Review createReview(User user, Book book, String content, double rating,
      OffsetDateTime createdAt) {
    Review review = new Review(book, user, content, rating);
    em.persist(review);
    em.flush();

    ReflectionTestUtils.setField(review, "createdAt", createdAt);
    em.flush();

    return review;
  }

  private User createUser(String email, String nickname, String password) {
    User user = new User(email, nickname, password);
    em.persist(user);
    return user;
  }

  private Book createBook(String title, String isbn) {
    Book book = Book.builder()
        .title(title)
        .author("테스트 저자")
        .description("테스트 설명")
        .publisher("테스트 출판사")
        .publishedDate(LocalDate.of(2024, 1, 1))
        .isbn(isbn)
        .thumbnailUrl("thumb.jpg")
        .build();

    em.persist(book);
    return book;
  }
}
