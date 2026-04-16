package com.team01.deokhugam.comment.dto;

import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public record CommentSearchCondition(
    UUID reviewId, Sort.Direction direction, String cursor, OffsetDateTime after, Integer limit) {

  public CommentSearchCondition {
    direction = (direction == null) ? Sort.Direction.DESC : direction;
  }

  public int normalizedLimit() {
    return PageLimitPolicy.normalize(limit);
  }
}
