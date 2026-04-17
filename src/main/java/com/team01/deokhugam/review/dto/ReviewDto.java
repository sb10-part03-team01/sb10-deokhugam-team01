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
    Double rating,
    long likeCount,
    long commentCount,
    boolean likedByMe,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

}