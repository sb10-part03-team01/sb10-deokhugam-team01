package com.team01.deokhugam.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.global.config.JpaConfig;
import com.team01.deokhugam.global.config.QueryDslConfig;
import com.team01.deokhugam.notification.entity.Notification;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class NotificationRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BookRepository bookRepository;
  @Autowired
  private ReviewRepository reviewRepository;
  @Autowired
  private NotificationRepository notificationRepository;


  private User savedUser;

  private Book savedBook;

  private Review savedReview;

  @BeforeEach
  void setUp() {
    savedUser = userRepository.save(new User("test@test.com", "test", "test1234!"));
    savedBook = bookRepository.save(new Book("testbook", "testAuthor", "test", "test",
        LocalDate.now(), "12345678910101010", null));
    savedReview = reviewRepository.save(new Review(savedBook, savedUser, "test", 1.0));
  }

  @Nested
  @DisplayName("알림 목록 조회 - DESC 정렬")
  class FindByUserIdOrderByCreatedAtDescTest {

    @Test
    @DisplayName("첫 페이지 조회 시 최신순으로 반환")
    void firstPageDesc() throws InterruptedException {
      //given
      notificationRepository.save(new Notification(savedReview, savedUser, "test1"));
      Thread.sleep(10);
      notificationRepository.save(new Notification(savedReview, savedUser, "test2"));
      Thread.sleep(10);
      notificationRepository.save(new Notification(savedReview, savedUser, "test3"));
      //when
      List<Notification> result =
          notificationRepository.findByUserIdOrderByCreatedAtDesc(savedUser.getId(),
              PageRequest.of(0, 10));
      //then
      assertThat(result).hasSize(3);
      assertThat(result.get(0).getContent()).isEqualTo("test3");
      assertThat(result.get(2).getContent()).isEqualTo("test1");

    }
  }

  @Nested
  @DisplayName("알림 목록 조회 - ASC 정렬")
  class FindByUserIdOrderByCreatedAtAscTest {

    @Test
    @DisplayName("첫 페이지 조회 시 오름차순으로 반환")
    void firstPageAsc() throws InterruptedException {
      //given
      notificationRepository.save(new Notification(savedReview, savedUser, "test1"));
      Thread.sleep(10);
      notificationRepository.save(new Notification(savedReview, savedUser, "test2"));
      Thread.sleep(10);
      notificationRepository.save(new Notification(savedReview, savedUser, "test3"));
      //when
      List<Notification> result =
          notificationRepository.findByUserIdOrderByCreatedAtAsc(savedUser.getId(),
              PageRequest.of(0, 10));
      //then
      assertThat(result).hasSize(3);
      assertThat(result.get(0).getContent()).isEqualTo("test1");
      assertThat(result.get(2).getContent()).isEqualTo("test3");

    }
  }

  @Nested
  @DisplayName("알림 목록 조회 - 커서 페이지네이션")
  class CursorPaginationTest {

    @Test
    @DisplayName("DESC - 커서 이전 데이터만 반환")
    void cursorDesc() throws InterruptedException {
      //given
      Notification n1 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test1"));
      Thread.sleep(10);
      Notification n2 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test2"));
      Thread.sleep(10);
      Notification n3 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test3"));

      notificationRepository.flush();

      Notification cursor = notificationRepository.findById(n2.getId()).get();

      //when
      List<Notification> result = notificationRepository
          .findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
              savedUser.getId(),
              cursor.getCreatedAt(),
              cursor.getId(),
              PageRequest.of(0, 2));

      //then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getContent()).isEqualTo("test1");
    }

    @Test
    @DisplayName("ASC - 커서 이후 데이터만 반환")
    void cursorAsc() throws InterruptedException {
      //given
      Notification n1 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test1"));
      Thread.sleep(10);
      Notification n2 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test2"));
      Thread.sleep(10);
      Notification n3 = notificationRepository.save(
          new Notification(savedReview, savedUser, "test3"));
      notificationRepository.flush();

      Notification cursor = notificationRepository.findById(n2.getId()).get();

      //when
      List<Notification> result = notificationRepository
          .findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(
              savedUser.getId(),
              cursor.getCreatedAt(),
              cursor.getId(),
              PageRequest.of(0, 2));

      //then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getContent()).isEqualTo("test3");
    }
  }
}
