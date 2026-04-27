package com.team01.deokhugam.dashboard.popularbook.controller;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularbook.dto.PopularBookDto;
import com.team01.deokhugam.dashboard.popularbook.service.PopularBookService;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PopularBookController implements PopularBookApi {

  private final PopularBookService popularBookService;

  @GetMapping("/books/popular")
  public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") DashboardPeriod period,
      @RequestParam(defaultValue = "ASC") SortDirection direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) OffsetDateTime after,
      @RequestParam(required = false) Integer limit) {

    log.info(
        "[DASHBOARD] getPopularBooks period={}, direction={}, cursor={}, after={}, limit={}",
        period,
        direction,
        cursor,
        after,
        limit);

    CursorPageResponse<PopularBookDto> response =
        popularBookService.findPopularBooks(period, direction, cursor, after, limit);
    return ResponseEntity.ok(response);
  }
}
