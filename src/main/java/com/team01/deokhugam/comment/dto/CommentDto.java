package com.team01.deokhugam.comment.dto;

import com.team01.deokhugam.comment.entity.Comment;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public static CommentDto from(Comment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getReview().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
