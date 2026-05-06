package com.team01.deokhugam.dashboard.popularreview.controller;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.global.enums.SortDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface PopularReviewApi {

  @Operation(summary = "인기 리뷰 목록 조회", description = "기간별 인기 리뷰 목록을 커서 페이지네이션 방식으로 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 리뷰 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponsePopularReviewDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @Parameter(description = "랭킹 기간: DAILY, WEEKLY, MONTHLY, ALL_TIME", example = "DAILY")
      DashboardPeriod period,

      @Parameter(description = "정렬 방향: ASC, DESC", example = "ASC")
      SortDirection direction,

      @Parameter(description = "다음 페이지 조회를 위한 커서")
      String cursor,

      @Parameter(description = "보조 커서 - 이전 페이지 마지막 항목의 생성 일시")
      OffsetDateTime after,

      @Parameter(description = "페이지 크기", example = "50")
      Integer limit
  );
}
