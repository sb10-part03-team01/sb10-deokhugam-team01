package com.team01.deokhugam.dashboard.popularreview.controller;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.CursorPageResponsePopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.service.PopularReviewService;
import com.team01.deokhugam.global.enums.SortDirection;
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
public class PopularReviewController implements PopularReviewApi {

  private final PopularReviewService popularReviewService;

  @Override
  @GetMapping("/popular")
  public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") DashboardPeriod period,
      @RequestParam(defaultValue = "ASC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime after,
      @RequestParam(defaultValue = "50") Integer limit
  ) {
    CursorPageResponsePopularReviewDto response =
        popularReviewService.getPopularReviews(period, direction, cursor, after, limit);

    log.info(
        "인기 리뷰 목록 조회 성공: period={}, direction={}, size={}, hasNext={}",
        period,
        direction,
        response.size(),
        response.hasNext()
    );

    return ResponseEntity.ok(response);
  }
}
