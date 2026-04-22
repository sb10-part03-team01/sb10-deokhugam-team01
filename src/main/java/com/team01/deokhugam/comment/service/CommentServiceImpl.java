package com.team01.deokhugam.comment.service;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.exception.comment.CommentNotFoundException;
import com.team01.deokhugam.global.exception.comment.ForbiddenCommentAccessException;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.review.repository.ReviewRepository;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  @Override
  public CommentDto createComment(UUID userId, CommentCreateRequest request) {
    log.info("[COMMENT] create userId={}, reviewId={}", userId, request.reviewId());

    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

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

    review.increaseCommentCount();

    return CommentDto.from(savedComment);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentDto getComment(UUID commentId) {
    Comment comment =
        commentRepository
            .findDetailById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
    return CommentDto.from(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<CommentDto> getComments(
      UUID reviewId, CursorPageRequest pageRequest, SortDirection direction) {
    if (!reviewRepository.existsById(reviewId)) {
      throw new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", reviewId));
    }

    CommentSearchCondition condition =
        new CommentSearchCondition(
            reviewId, direction, pageRequest.cursor(), pageRequest.after(), pageRequest.limit());

    log.debug(
        "[COMMENT] getComments reviewId={}, direction={}, cursor={}, after={}, limit={}",
        reviewId,
        direction,
        pageRequest.cursor(),
        pageRequest.after(),
        condition.normalizedLimit());

    List<Comment> comments = commentRepository.findAllByCursor(condition);
    long totalElements = commentRepository.countCommentsByReviewId(reviewId);

    List<CommentDto> content = comments.stream().map(CommentDto::from).toList();

    return CursorPaginationUtils.of(
        content,
        condition.normalizedLimit(),
        totalElements,
        dto -> dto.id().toString(),
        CommentDto::createdAt);
  }

  @Override
  public CommentDto updateComment(UUID userId, UUID commentId, CommentUpdateRequest request) {
    log.info("[COMMENT] update commentId={}, userId={}", commentId, userId);

    Comment comment =
        commentRepository
            .findDetailById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

    // 본인이 쓴 댓글 맞는지 확인
    validateOwner(userId, comment);

    String content = request.content().trim();
    comment.updateContent(content);

    // fetch join 사용
    return CommentDto.from(comment);
  }

  // Soft Delete
  @Override
  public void deleteComment(UUID userId, UUID commentId) {
    log.info("[COMMENT] softDelete commentId={}, userId={}", commentId, userId);

    Comment comment =
        commentRepository
            .findDetailById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

    validateOwner(userId, comment);

    // 논리 삭제
    comment.softDelete();
    comment.getReview().decreaseCommentCount();
  }

  // Hard Delete
  @Override
  public void hardDeleteComment(UUID userId, UUID commentId) {
    log.info("[COMMENT] hardDelete commentId={}, userId={}", commentId, userId);

    Comment comment =
        commentRepository
            .findByIdAndIsDeletedTrue(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

    validateOwner(userId, comment);

    commentRepository.delete(comment);
  }

  // 요청자가 댓글 작성자인지 검증
  private void validateOwner(UUID userId, Comment comment) {
    if (!comment.getUser().getId().equals(userId)) {
      log.warn(
          "[COMMENT] forbidden commentId={}, requestUserId={}, ownerUserId={}",
          comment.getId(),
          userId,
          comment.getUser().getId());
      throw new ForbiddenCommentAccessException(userId, comment.getId());
    }
  }
}
