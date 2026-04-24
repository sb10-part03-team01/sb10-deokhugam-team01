package com.team01.deokhugam.review.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewDto(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String content,
    double rating,
    int likeCount,
    int commentCount,
    boolean likedByMe,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  public ReviewDto withLikedByMe(boolean likedByMe) {
    return new ReviewDto(
        id,
        bookId,
        bookTitle,
        bookThumbnailUrl,
        userId,
        userNickname,
        content,
        rating,
        likeCount,
        commentCount,
        likedByMe,
        createdAt,
        updatedAt
    );
  }
}
