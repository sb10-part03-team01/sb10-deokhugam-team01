package com.team01.deokhugam.comment.dto;

import com.team01.deokhugam.global.enums.SortDirection;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchRequest {

  private UUID reviewId;
  private SortDirection direction = SortDirection.DESC;
  private String cursor;
  private OffsetDateTime after;
  private Integer limit = 50;
}
