package com.team01.deokhugam.dashboard.popularreview.dto;

import java.util.UUID;

public record PopularReviewScoreRow(
    UUID reviewId,
    long likeCount,
    long commentCount
) {

  public double score() {
    return likeCount * 0.3 + commentCount * 0.7;
  }
}
