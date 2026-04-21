package com.team01.deokhugam.review.dto;

import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewSearchCondition(
    UUID userId,
    UUID bookId,
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    OffsetDateTime after,
    Integer limit
) {

  public ReviewSearchCondition {
    orderBy = (orderBy == null || orderBy.isBlank()) ? "createdAt" : orderBy;
    direction = (direction == null || direction.isBlank()) ? "DESC" : direction.toUpperCase();
  }

  public int normalizedLimit() {
    return PageLimitPolicy.normalize(limit);
  }

}
