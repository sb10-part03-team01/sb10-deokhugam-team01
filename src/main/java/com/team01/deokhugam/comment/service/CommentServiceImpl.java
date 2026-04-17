package com.team01.deokhugam.comment.service;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CommentDto createComment(UUID userId, CommentCreateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new DeokhugamException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

    Review review =
        reviewRepository
            .findById(request.reviewId())
            .orElseThrow(
                () ->
                    new DeokhugamException(
                        ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", request.reviewId())));

    String content = request.content();

    Comment comment = new Comment(review, user, content);
    Comment savedComment = commentRepository.save(comment);

    return CommentDto.from(savedComment);
  }

  @Override
  public CommentDto getComment(UUID commentId) {
    Comment comment =
        commentRepository
            .findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(
                () ->
                    new DeokhugamException(
                        ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId)));

    return CommentDto.from(comment);
  }

  @Override
  public CursorPageResponse<CommentDto> getComments(
      UUID reviewId, CursorPageRequest pageRequest, Sort.Direction direction) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  @Transactional
  public CommentDto updateComment(UUID userId, UUID commentId, CommentUpdateRequest request) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  @Transactional
  public void deleteComment(UUID userId, UUID commentId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
