package com.team01.deokhugam.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

}
