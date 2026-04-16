package com.team01.deokhugam.comment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
