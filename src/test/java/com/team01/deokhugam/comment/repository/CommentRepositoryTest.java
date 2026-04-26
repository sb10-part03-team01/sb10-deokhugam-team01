package com.team01.deokhugam.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

// PostgreSQL용 schema.sql을 H2에 그대로 실행하지 않도록 막고,
// QueryDSL 테스트용 JPAQueryFactory 빈을 함께 주입한다.
@Import(QuerydslTestConfig.class)
@DataJpaTest
@TestPropertySource(
    properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CommentRepositoryTest {

  @Autowired private CommentRepository commentRepository;

  @Autowired private EntityManager em;

  @Test
  @DisplayName("findDetailById - 삭제되지 않은 댓글은 작성자와 리뷰를 함께 조회한다")
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
  @DisplayName("findDetailById - 논리 삭제된 댓글은 조회되지 않는다")
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
  @DisplayName("findAllByCursor - 첫 페이지 DESC 조회 시 최신 댓글부터 limit + 1개를 가져온다")
  void find_all_by_cursor_first_page_desc() {
    // given
    User user = persistUser("user@test.com", "user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    Comment oldest =
        persistComment(review, user, "댓글1", time(2026, 4, 20, 9, 0), time(2026, 4, 20, 9, 0));
    Comment older =
        persistComment(review, user, "댓글2", time(2026, 4, 20, 10, 0), time(2026, 4, 20, 10, 0));
    Comment middle =
        persistComment(review, user, "댓글3", time(2026, 4, 20, 11, 0), time(2026, 4, 20, 11, 0));
    Comment latest =
        persistComment(review, user, "댓글4", time(2026, 4, 20, 12, 0), time(2026, 4, 20, 12, 0));

    CommentSearchCondition condition =
        new CommentSearchCondition(review.getId(), SortDirection.DESC, null, null, 2);

    em.flush();
    em.clear();

    // when
    List<Comment> result = commentRepository.findAllByCursor(condition);

    // then
    // limit = 2 + 1개를 가져와 서비스 계층에서 hasNext를 계산
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(latest.getId());
    assertThat(result.get(1).getId()).isEqualTo(middle.getId());
    assertThat(result.get(2).getId()).isEqualTo(older.getId());
    assertThat(result).extracting(Comment::getId).doesNotContain(oldest.getId());
  }

  @Test
  @DisplayName("findAllByCursor - 다음 페이지 조회 시 after와 cursor를 기준으로 이어서 조회한다")
  void find_all_by_cursor_next_page_desc() {
    // given
    User user = persistUser("cursor@test.com", "cursor-user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    Comment oldest =
        persistComment(review, user, "댓글1", time(2026, 4, 20, 9, 0), time(2026, 4, 20, 9, 0));
    Comment older =
        persistComment(review, user, "댓글2", time(2026, 4, 20, 10, 0), time(2026, 4, 20, 10, 0));
    Comment middle =
        persistComment(review, user, "댓글3", time(2026, 4, 20, 11, 0), time(2026, 4, 20, 11, 0));
    persistComment(review, user, "댓글4", time(2026, 4, 20, 12, 0), time(2026, 4, 20, 12, 0));

    // 첫 페이지 결과가 [12:00, 11:00] 이라고 가정하면,
    // 마지막 요소인 middle을 커서로 넘겼을 때 그 다음 댓글부터 조회되어야 한다.
    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(),
            SortDirection.DESC,
            middle.getId().toString(),
            middle.getCreatedAt(),
            2);

    em.flush();
    em.clear();

    // when
    List<Comment> result = commentRepository.findAllByCursor(condition);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(older.getId());
    assertThat(result.get(1).getId()).isEqualTo(oldest.getId());
  }

  @Test
  @DisplayName("findAllByCursor - createdAt이 같으면 id를 보조 커서로 사용한다")
  void find_all_by_cursor_uses_id_as_tie_breaker() {
    // given
    User user = persistUser("tie@test.com", "tie-user");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    Comment sameTimeComment1 =
        persistComment(review, user, "댓글A", time(2026, 4, 20, 12, 0), time(2026, 4, 20, 12, 0));
    Comment sameTimeComment2 =
        persistComment(review, user, "댓글B", time(2026, 4, 20, 12, 0), time(2026, 4, 20, 12, 0));
    Comment older =
        persistComment(review, user, "댓글C", time(2026, 4, 20, 11, 0), time(2026, 4, 20, 11, 0));

    em.flush();

    Comment highIdComment =
        sameTimeComment1.getId().compareTo(sameTimeComment2.getId()) > 0
            ? sameTimeComment1
            : sameTimeComment2;

    Comment lowIdComment =
        sameTimeComment1.getId().compareTo(sameTimeComment2.getId()) > 0
            ? sameTimeComment2
            : sameTimeComment1;

    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(),
            SortDirection.DESC,
            highIdComment.getId().toString(),
            highIdComment.getCreatedAt(),
            2);

    em.clear();

    // when
    List<Comment> result = commentRepository.findAllByCursor(condition);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(lowIdComment.getId());
    assertThat(result.get(1).getId()).isEqualTo(older.getId());
  }

  @Test
  @DisplayName("findAllByCursor - cursor만 있으면 INVALID_CURSOR_PAGINATION 예외가 발생한다")
  void find_all_by_cursor_invalid_cursor_only() {
    // given
    User user = persistUser("user3@test.com", "user3");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(), SortDirection.DESC, UUID.randomUUID().toString(), null, 2);

    // when // then
    assertThatThrownBy(() -> commentRepository.findAllByCursor(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception ->
                assertThat(((DeokhugamException) exception).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION));
  }

  @Test
  @DisplayName("findAllByCursor - after만 있으면 INVALID_CURSOR_PAGINATION 예외가 발생한다")
  void find_all_by_cursor_invalid_after_only() {
    // given
    User user = persistUser("user4@test.com", "user4");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(), SortDirection.DESC, null, time(2026, 4, 20, 11, 0), 2);

    // when // then
    assertThatThrownBy(() -> commentRepository.findAllByCursor(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception ->
                assertThat(((DeokhugamException) exception).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_CURSOR_PAGINATION));
  }

  @Test
  @DisplayName("findAllByCursor - cursor가 UUID 형식이 아니면 INVALID_CURSOR_FORMAT 예외가 발생한다")
  void find_all_by_cursor_invalid_cursor_format() {
    // given
    User user = persistUser("user5@test.com", "user5");
    Book book = persistBook();
    Review review = persistReview(user, book, "리뷰");

    CommentSearchCondition condition =
        new CommentSearchCondition(
            review.getId(), SortDirection.DESC, "not-a-uuid", time(2026, 4, 20, 11, 0), 2);

    // when // then
    assertThatThrownBy(() -> commentRepository.findAllByCursor(condition))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception ->
                assertThat(((DeokhugamException) exception).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT));
  }

  @Test
  @DisplayName("countCommentsByReviewId - 논리 삭제된 댓글은 제외하고 카운트한다")
  void count_comments_by_review_id_excludes_deleted() {
    // given
    User user = persistUser("user6@test.com", "user6");
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
        new Book(
            "테스트 제목", "테스트 저자", "설명입니다", "출판사", LocalDate.now(), "2123213123123213", "thumbnail");

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

  private Comment persistComment(
      Review review,
      User user,
      String content,
      UUID id,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    Comment comment = new Comment(review, user, content);

    setField(comment, "id", id);
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
