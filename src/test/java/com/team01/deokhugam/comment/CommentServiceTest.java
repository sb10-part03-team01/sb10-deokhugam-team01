package com.team01.deokhugam.comment;

import static org.mockito.BDDMockito.given;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.comment.service.CommentService;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
  @InjectMocks private CommentService commentService;

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
  void createComment() {
    CommentCreateRequest commentCreateRequest = new CommentCreateRequest(reviewId, "댓글 내용");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
  }
}
