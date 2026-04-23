package com.team01.deokhugam.dashboard.controller;

import com.team01.deokhugam.dashboard.dto.PopularBookDto;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface DashboardApi {

  @Operation(summary = "인기 도서 목록 조회", description = "기간별 인기 도서 목록을 조회합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "인기 도서 목록 조회 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CursorPageResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(mediaType = "application/json"))
      })
  ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @Parameter(description = "랭킹 기간", example = "DAILY") RankingPeriod period,
      @Parameter(description = "정렬 방향", example = "DESC") SortDirection direction,
      @Parameter(description = "커서 페이지네이션 커서") String cursor,
      @Parameter(description = "보조 커서(createdAt)") OffsetDateTime after,
      @Parameter(description = "페이지 크기", example = "50") Integer limit);
}
