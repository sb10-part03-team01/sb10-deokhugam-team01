package com.team01.deokhugam.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.comment.service.CommentServiceImpl;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
  @Mock private CommentRepository commentRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private CommentServiceImpl commentService;

  private UUID userId;
  private UUID bookId;
  private UUID reviewId;
  private User user;
  private Review review;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    bookId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    user = new User("jongin@test.com", "jongin", "1234");
    review = new Review(bookId, userId, "hello", 4.3);
  }

  @Test
  @DisplayName("댓글 생성 테스트")
  void createComment() {
    CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, "댓글 내용");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(commentRepository.save(any(Comment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    CommentDto result = commentService.createComment(userId, commentCreateRequest);

    assertThat(result.content()).isEqualTo("댓글 내용");
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
  @DisplayName("댓글 생성시 리뷰가 없으면 예외를 발생시킨다.")
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
}
