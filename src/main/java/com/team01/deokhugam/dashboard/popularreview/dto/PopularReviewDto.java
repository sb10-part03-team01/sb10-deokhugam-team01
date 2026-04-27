package com.team01.deokhugam.dashboard.popularreview.dto;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    DashboardPeriod period,
    OffsetDateTime createdAt,
    int rank,
    double score,
    int likeCount,
    int commentCount
) {

}
