package com.team01.deokhugam.comment.repository;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.config.QuerydslTestConfig;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@Import(QuerydslTestConfig.class)
@DataJpaTest
@TestPropertySource(
    properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
public class CommentRepositoryTest {

  @Autowired private CommentRepository commentRepository;

  @Autowired private EntityManager em;

  @Test
  @DisplayName("findDetailById - 댓글 상세 조회 성공")
  void find_detail_by_id_success() {
    // given
    User user = persistUser("jongin@test.com", "jongin");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");
    Comment comment = persistComment(review, user, "댓글");

    em.flush();
    em.clear();

    // when
    Optional<Comment> result = commentRepository.findDetailById(comment.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(comment.getId());
    assertThat(result.get().getContent()).isEqualTo("댓글");
    assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    assertThat(result.get().getReview().getId()).isEqualTo(review.getId());
  }

  @Test
  @DisplayName("findDetailById - 논리 삭제된 댓글은 조회되지 않음")
  void find_detail_by_id_deleted_comment_not_found() {
    // given
    User user = persistUser("user@test.com", "user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");
    Comment comment = persistDeletedComment(review, user, "삭제된 댓글");

    em.flush();
    em.clear();

    // when
    Optional<Comment> result = commentRepository.findDetailById(comment.getId());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findAllByCursor - 첫 페이지 DESC 조회 성공")
  void find_all_by_cursor_first_page_desc() {
    // given
    User user = persistUser("user@test.com", "user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    Comment oldest =
        persistComment(review, user, "댓글1", time(2026, 4, 20, 10, 0), time(2026, 4, 20, 10, 0));
    Comment middle =
        persistComment(review, user, "댓글2", time(2026, 4, 20, 11, 0), time(2026, 4, 20, 11, 0));
    Comment latest =
        persistComment(review, user, "댓글3", time(2026, 4, 20, 12, 0), time(2026, 4, 20, 12, 0));

    CommentSearchCondition condition =
        new CommentSearchCondition(review.getId(), SortDirection.DESC, null, null, 2);

    em.flush();
    em.clear();

    // when
    List<Comment> result = commentRepository.findAllByCursor(condition);

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(latest.getId());
    assertThat(result.get(1).getId()).isEqualTo(middle.getId());
    assertThat(result.get(2).getId()).isEqualTo(oldest.getId());
  }

  @Test
  @DisplayName("findAllByCursor - cursor와 after 중 하나만 있으면 예외 발생")
  void find_all_by_cursor_invalid_cursor_pagination() {
    // given
    User user = persistUser("user@test.com", "user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(), SortDirection.DESC, UUID.randomUUID().toString(), null, 2);

    // when // then
    assertThatThrownBy(() -> commentRepository.findAllByCursor(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION);
            });
  }

  @Test
  @DisplayName("countCommentsByReviewId - 논리 삭제된 댓글은 제외하고 카운트")
  void count_comments_by_review_id_excludes_deleted() {
    // given
    User user = persistUser("user7@test.com", "user7");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    persistComment(review, user, "댓글1", time(2026, 4, 20, 10, 0), time(2026, 4, 20, 10, 0));
    persistComment(review, user, "댓글2", time(2026, 4, 20, 11, 0), time(2026, 4, 20, 11, 0));
    persistDeletedComment(review, user, "삭제된 댓글");

    em.flush();
    em.clear();

    // when
    long count = commentRepository.countCommentsByReviewId(review.getId());

    // then
    assertThat(count).isEqualTo(2);
  }

  // 생성 임시 메서드
  private User persistUser(String email, String nickname) {
    User user = new User(email, nickname, "1234");

    OffsetDateTime now = OffsetDateTime.now();

    ReflectionTestUtils.setField(user, "createdAt", now);
    ReflectionTestUtils.setField(user, "updatedAt", now);

    em.persist(user);
    return user;
  }

  private Book persistBook() {
    Book book =
        new Book("테스트 제목", "테스트 저자", "설명입니다", "출판사", LocalDate.now(), "2123213123123213", "ds");

    OffsetDateTime now = OffsetDateTime.now();
    setField(book, "createdAt", now);
    setField(book, "updatedAt", now);

    em.persist(book);
    return book;
  }

  private Review persistReview(User user, Book book, String content) {
    Review review = new Review(book, user, content, 4.5);

    OffsetDateTime now = OffsetDateTime.now();
    setField(review, "createdAt", now);
    setField(review, "updatedAt", now);

    em.persist(review);
    return review;
  }

  private Comment persistComment(Review review, User user, String content) {
    Comment comment = new Comment(review, user, content);

    OffsetDateTime now = OffsetDateTime.now();

    setField(comment, "createdAt", now);
    setField(comment, "updatedAt", now);
    setField(comment, "isDeleted", false);

    em.persist(comment);
    return comment;
  }

  private Comment persistComment(
      Review review,
      User user,
      String content,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {

    Comment comment = new Comment(review, user, content);

    setField(comment, "createdAt", createdAt);
    setField(comment, "updatedAt", updatedAt);
    setField(comment, "isDeleted", false);

    em.persist(comment);
    return comment;
  }

  private Comment persistDeletedComment(Review review, User user, String content) {
    Comment comment = new Comment(review, user, content);

    OffsetDateTime now = OffsetDateTime.now();

    setField(comment, "createdAt", now.minusMinutes(5));
    setField(comment, "updatedAt", now.minusMinutes(5));
    setField(comment, "isDeleted", true);
    setField(comment, "deletedAt", now);

    em.persist(comment);
    return comment;
  }

  private OffsetDateTime time(int year, int month, int day, int hour, int minute) {
    return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
  }
}
