package com.team01.deokhugam.review.dto;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewSearchCondition(
    UUID userId,
    UUID bookId,
    String keyword,
    String orderBy,
    SortDirection direction,
    String cursor,
    OffsetDateTime after,
    Integer limit
) {

  public ReviewSearchCondition {
    orderBy = normalizeOrderBy(orderBy);
    direction = (direction == null) ? SortDirection.DESC : direction;
  }

  private static String normalizeOrderBy(String orderBy) {
    if (orderBy == null || orderBy.isBlank()) {
      return "createdAt";
    }
    if ("createdAt".equalsIgnoreCase(orderBy)) {
      return "createdAt";
    }
    if ("rating".equalsIgnoreCase(orderBy)) {
      return "rating";
    }
    throw new IllegalArgumentException("orderBy는 createdAt 또는 rating만 가능합니다.");
  }

  public int normalizedLimit() {
    return PageLimitPolicy.normalize(limit);
  }
}
