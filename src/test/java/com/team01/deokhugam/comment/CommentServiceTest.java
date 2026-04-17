package com.team01.deokhugam.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.team01.deokhugam.book.Book;
import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.comment.service.CommentServiceImpl;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
  @Mock private CommentRepository commentRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private CommentServiceImpl commentService;

  private UUID userId;
  private UUID otherUserId;
  private UUID bookId;
  private UUID reviewId;
  private UUID commentId;

  private User user;
  private User otherUser;
  private Review review;
  private Comment comment;
  private Book book;

  @BeforeEach
  void setUp() throws Exception {
    userId = UUID.randomUUID();
    otherUserId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    bookId = UUID.randomUUID();

    user = new User("jongin@test.com", "jongin", "1234");
    otherUser = new User("torie@test.com", "torie", "1234");
    book = new Book("Hello", "123", "123", "123", LocalDate.now(), "123", "123");
    review = new Review(book, user, "hello", 4.3);
    comment = new Comment(review, user, "기존 댓글");

    setField(user, "id", userId);
    setField(otherUser, "id", otherUserId);

    setField(review, "id", reviewId);

    setField(comment, "id", commentId);

    OffsetDateTime baseTime = OffsetDateTime.of(2026, 4, 17, 12, 0, 0, 0, ZoneOffset.UTC);
    setField(comment, "createdAt", baseTime);
    setField(comment, "updatedAt", baseTime.plusMinutes(5));
  }

  @Test
  @DisplayName("댓글 생성 테스트")
  void createComment() {
    // given
    CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, "댓글 내용");
    int beforeCount = review.getCommentCount();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(commentRepository.save(any(Comment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    CommentDto result = commentService.createComment(userId, commentCreateRequest);

    // then
    assertThat(result.content()).isEqualTo("댓글 내용");
    assertThat(review.getCommentCount()).isEqualTo(beforeCount + 1);
  }

  @Test
  @DisplayName("댓글 생성시 리뷰가 없으면 예외를 발생시킨다.")
  void create_comment_null_review_exception() {

    // given, when
    CommentCreateRequest request = new CommentCreateRequest(reviewId, "댓글 내용");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    // then
    assertThatThrownBy(() -> commentService.createComment(userId, request))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
  }

  @Test
  @DisplayName("댓글 생성시 유저가 없으면 예외를 발생시킨다.")
  void create_comment_null_user_exception() {

    // given, when
    CommentCreateRequest request = new CommentCreateRequest(reviewId, "댓글 내용");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // then
    assertThatThrownBy(() -> commentService.createComment(userId, request))
        .isInstanceOf(DeokhugamException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("단건 조회 테스트")
  void get_comment_by_id() {
    // given
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.of(comment));

    // when
    CommentDto result = commentService.getComment(commentId);

    // then
    assertThat(result.id()).isEqualTo(commentId);
    assertThat(result.reviewId()).isEqualTo(reviewId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.userNickname()).isEqualTo("jongin");
    assertThat(result.content()).isEqualTo("기존 댓글");
  }

  @Test
  @DisplayName("댓글 단건 조회 시 댓글 없으면 예외 발생")
  void get_comment_by_id_null_comment_exception() {
    // given
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.empty());

    // when, then
    assertThatThrownBy(() -> commentService.getComment(commentId))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
            });
  }

  @Test
  @DisplayName("댓글 수정")
  void update_comment() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.of(comment));

    // when
    CommentDto result = commentService.updateComment(userId, commentId, request);

    // then
    assertThat(comment.getContent()).isEqualTo("수정된 댓글");
    assertThat(result.id()).isEqualTo(commentId);
  }

  @Test
  @DisplayName("댓글 수정시 댓글이 없으면 예외가 발생한다")
  void update_comment_null_exception() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.empty());

    // when // then
    assertThatThrownBy(() -> commentService.updateComment(userId, commentId, request))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
            });
  }

  @Test
  @DisplayName("댓글 수정시 작성자가 아니면 예외가 발생한다")
  void update_comment_not_author_exception() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글");
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.of(comment));

    // when then
    assertThatThrownBy(() -> commentService.updateComment(otherUserId, commentId, request))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_COMMENT_ACCESS);
            });
  }

  @Test
  @DisplayName("댓글 삭제")
  void delete_comment() {
    // given
    review.increaseCommentCount();
    int beforeCount = review.getCommentCount();

    given(commentRepository.findDetailById(commentId)).willReturn(Optional.of(comment));

    // when
    commentService.deleteComment(userId, commentId);

    // then
    assertThat(comment.isDeleted()).isTrue();
    assertThat(comment.getDeletedAt()).isNotNull();
    assertThat(review.getCommentCount()).isEqualTo(beforeCount - 1);
  }

  @Test
  @DisplayName("댓글 삭제시 작성자가 아니면 예외 발생")
  void delete_not_author_exception() {
    // given
    given(commentRepository.findDetailById(commentId)).willReturn(Optional.of(comment));

    // when // then
    assertThatThrownBy(() -> commentService.deleteComment(otherUserId, commentId))
        .isInstanceOf(DeokhugamException.class)
        .satisfies(
            exception -> {
              DeokhugamException e = (DeokhugamException) exception;
              assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_COMMENT_ACCESS);
            });
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void get_comments_success() {
    // given
    UUID reviewID = this.reviewId;
    // 첫 페이지
    CursorPageRequest pageRequest = new CursorPageRequest(null, null, 2);

    Comment comment1 = new Comment(review, user, "댓글1");
    Comment comment2 = new Comment(review, user, "댓글2");
    Comment comment3 = new Comment(review, user, "댓글3");
    setField(comment1, "id", UUID.randomUUID());
    setField(comment2, "id", UUID.randomUUID());
    setField(comment3, "id", UUID.randomUUID());

    OffsetDateTime now = OffsetDateTime.now();

    setField(comment1, "createdAt", now.minusMinutes(3));
    setField(comment2, "createdAt", now.minusMinutes(2));
    setField(comment3, "createdAt", now.minusMinutes(1));

    setField(comment1, "updatedAt", now);
    setField(comment2, "updatedAt", now);
    setField(comment3, "updatedAt", now);

    given(reviewRepository.existsById(reviewId)).willReturn(true);

    // limit = 2로
    given(commentRepository.findAllByCursor(any()))
        .willReturn(java.util.List.of(comment1, comment2, comment3));

    given(commentRepository.countCommentsByReviewId(reviewId)).willReturn(3L);

    // when
    var result = commentService.getComments(reviewId, pageRequest, Sort.Direction.DESC);

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextAfter()).isNotNull();
    assertThat(result.totalElements()).isEqualTo(3L);
  }
}
