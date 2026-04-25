package com.team01.deokhugam.comment.dto;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentSearchCondition(
    UUID reviewId, SortDirection direction, String cursor, OffsetDateTime after, Integer limit) {

  public CommentSearchCondition {
    direction = (direction == null) ? SortDirection.DESC : direction;
  }

  public int normalizedLimit() {
    return PageLimitPolicy.normalize(limit);
  }
}
