package com.team01.deokhugam.comment.service;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentSearchCondition;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.comment.entity.Comment;
import com.team01.deokhugam.comment.repository.CommentRepository;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
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
  @Transactional(readOnly = true)
  public CommentDto getComment(UUID commentId) {
    Comment comment =
        commentRepository
            .findDetailById(commentId)
            .orElseThrow(
                () ->
                    new DeokhugamException(
                        ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId)));
    // fetch join 필요
    return CommentDto.from(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<CommentDto> getComments(
      UUID reviewId, CursorPageRequest pageRequest, Sort.Direction direction) {
    if (!reviewRepository.existsById(reviewId)) {
      throw new DeokhugamException(ErrorCode.REVIEW_NOT_FOUND, Map.of("reviewId", reviewId));
    }

    CommentSearchCondition condition =
        new CommentSearchCondition(
            reviewId, direction, pageRequest.cursor(), pageRequest.after(), pageRequest.limit());

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
    Comment comment =
        commentRepository
            .findDetailById(commentId)
            .orElseThrow(
                () ->
                    new DeokhugamException(
                        ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId)));

    // 본인이 쓴 댓글 맞는지 확인
    validateOwner(userId, comment);
    String content = request.content().trim();
    comment.updateContent(content);

    // fetch join 사용
    return CommentDto.from(comment);
  }

  @Override
  public void deleteComment(UUID userId, UUID commentId) {
    Comment comment =
        commentRepository
            .findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(
                () ->
                    new DeokhugamException(
                        ErrorCode.COMMENT_NOT_FOUND, Map.of("commentId", commentId)));

    validateOwner(userId, comment);
    // 논리 삭제
    comment.softDelete();
  }

  // 댓글 단 id와 같은지 검사하는 메서드
  private void validateOwner(UUID userId, Comment comment) {
    if (!comment.getUser().getId().equals(userId)) {
      throw new DeokhugamException(
          ErrorCode.FORBIDDEN_COMMENT_ACCESS,
          Map.of("userId", userId, "commentId", comment.getId()));
    }
  }
}
