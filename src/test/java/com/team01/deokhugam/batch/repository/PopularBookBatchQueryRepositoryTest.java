package com.team01.deokhugam.batch.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.config.QuerydslTestConfig;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(QuerydslTestConfig.class)
@DataJpaTest
@TestPropertySource(
    properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class PopularBookBatchQueryRepositoryTest {

  @Autowired private PopularBookBatchQueryRepository popularBookBatchQueryRepository;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("기간 내 리뷰를 책별로 집계해 인기 도서 점수를 계산한다")
  void find_popular_books_between_success() {
    // given
    User user = persistUser("user@test.com", "user");

    Book book1 = persistBook("책1", "1111111111111");
    Book book2 = persistBook("책2", "2222222222222");

    OffsetDateTime start = time(2026, 4, 20, 0, 0);
    OffsetDateTime end = time(2026, 4, 21, 0, 0);

    // book1: 리뷰 2개, 평균 평점 4.5 -> 점수 = 2 * 0.4 + 4.5 * 0.6 = 3.5
    persistReview(book1, user, "리뷰1", 5.0, time(2026, 4, 20, 10, 0));
    persistReview(book1, user, "리뷰2", 4.0, time(2026, 4, 20, 11, 0));

    // book2: 리뷰 1개, 평균 평점 3.0 -> 점수 = 1 * 0.4 + 3.0 * 0.6 = 2.2
    persistReview(book2, user, "리뷰3", 3.0, time(2026, 4, 20, 12, 0));

    // 기간 밖 데이터는 집계에서 제외되어야 한다.
    persistReview(book2, user, "리뷰4", 5.0, time(2026, 4, 21, 1, 0));

    em.flush();
    em.clear();

    // when
    List<PopularBookScoreRow> result =
        popularBookBatchQueryRepository.findPopularBooksBetween(start, end);

    // then
    assertThat(result).hasSize(2);

    PopularBookScoreRow first = result.get(0);
    PopularBookScoreRow second = result.get(1);

    // 점수가 더 높은 book1이 먼저 조회되어야 한다.
    assertThat(first.bookId()).isEqualTo(book1.getId());
    assertThat(first.reviewCount()).isEqualTo(2L);
    assertThat(first.averageRating()).isCloseTo(4.5, within(0.0001));
    assertThat(first.score()).isCloseTo(3.5, within(0.0001));

    assertThat(second.bookId()).isEqualTo(book2.getId());
    assertThat(second.reviewCount()).isEqualTo(1L);
    assertThat(second.averageRating()).isCloseTo(3.0, within(0.0001));
    assertThat(second.score()).isCloseTo(2.2, within(0.0001));
  }

  private User persistUser(String email, String nickname) {
    User user = new User(email, nickname, "1234");

    OffsetDateTime now = OffsetDateTime.now();
    setField(user, "createdAt", now);
    setField(user, "updatedAt", now);

    em.persist(user);
    return user;
  }

  private Book persistBook(String title, String isbn) {
    Book book = new Book(title, "저자", "설명", "출판사", LocalDate.of(2026, 4, 1), isbn, "thumbnail");

    OffsetDateTime now = OffsetDateTime.now();
    setField(book, "createdAt", now);
    setField(book, "updatedAt", now);
    setField(book, "isDeleted", false);

    em.persist(book);
    return book;
  }

  private Review persistReview(
      Book book, User user, String content, double rating, OffsetDateTime createdAt) {
    Review review = new Review(book, user, content, rating);

    // insert 시 NOT NULL 제약 통과용
    setField(review, "createdAt", createdAt);
    setField(review, "updatedAt", createdAt);
    setField(review, "isDeleted", false);

    em.persist(review);
    em.flush();

    // Auditing이 덮었을 가능성 대비해서 DB 값을 직접 고정
    em.createQuery(
            """
            update Review r
            set r.createdAt = :createdAt,
                r.updatedAt = :updatedAt
            where r.id = :id
            """)
        .setParameter("createdAt", createdAt)
        .setParameter("updatedAt", createdAt)
        .setParameter("id", review.getId())
        .executeUpdate();

    em.flush();
    em.clear();

    return em.find(Review.class, review.getId());
  }

  private OffsetDateTime time(int year, int month, int day, int hour, int minute) {
    return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
  }
}
