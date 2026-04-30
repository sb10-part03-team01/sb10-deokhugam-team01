package com.team01.deokhugam.comment.dto;

import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchRequest {

  @NotNull
  @Schema(description = "리뷰 ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID reviewId;

  @Schema(description = "정렬 방향", example = "DESC")
  private SortDirection direction = SortDirection.DESC;

  @Schema(description = "커서 페이지네이션 커서")
  private String cursor;

  @Schema(description = "보조 커서(createdAt)")
  private OffsetDateTime after;

  @Schema(description = "페이지 크기", example = "50")
  private Integer limit = PageLimitPolicy.DEFAULT_LIMIT;
}
