package com.team01.deokhugam.dashboard.popularreview.controller;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.service.PopularReviewService;
import com.team01.deokhugam.global.enums.SortDirection;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class PopularReviewController {

  private final PopularReviewService popularReviewService;

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 리뷰 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/popular")
  public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") DashboardPeriod period,
      @RequestParam(defaultValue = "ASC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime after,
      @RequestParam(defaultValue = "50") Integer limit
  ) {
    return ResponseEntity.ok(
        popularReviewService.getPopularReviews(period, direction, cursor, after, limit)
    );
  }

}
