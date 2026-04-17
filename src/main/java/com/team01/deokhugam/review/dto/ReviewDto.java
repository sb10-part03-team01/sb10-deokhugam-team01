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

}