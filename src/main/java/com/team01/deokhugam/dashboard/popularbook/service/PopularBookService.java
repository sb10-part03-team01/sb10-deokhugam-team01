package com.team01.deokhugam.dashboard.popularbook.service;

import com.team01.deokhugam.dashboard.popularbook.dto.PopularBookDto;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.dashboard.popularbook.repository.PopularBookRepository;
import com.team01.deokhugam.global.enums.RankingPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopularBookService {

  private final PopularBookRepository popularBookRepository;

  // 인기 도서 조회
  @Transactional(readOnly = true)
  public CursorPageResponse<PopularBookDto> findPopularBooks(
      RankingPeriod period,
      SortDirection direction,
      String cursor,
      OffsetDateTime after,
      Integer limit) {
    // limit 보정
    int normalizedLimit = PageLimitPolicy.normalize(limit);

    List<PopularBook> popularBooks =
        popularBookRepository.findAllByCursor(period, direction, cursor, after, normalizedLimit);

    // calculatedDate 기준 인기 도서 개수
    long totalElements = popularBookRepository.countByPeriod(period);

    // 다음 페이지 여부
    boolean hasNext = popularBooks.size() > normalizedLimit;

    // 목록 생성
    List<PopularBookDto> content =
        popularBooks.stream().limit(normalizedLimit).map(PopularBookDto::from).toList();

    String nextCursor = null;
    OffsetDateTime nextAfter = null;

    // 다음 커서 계산
    if (hasNext && !content.isEmpty()) {
      PopularBookDto lastElement = content.get(content.size() - 1);
      nextCursor = String.valueOf(lastElement.getRank());
      nextAfter = lastElement.getCreatedAt();
    }

    return new CursorPageResponse<>(
        content, nextCursor, nextAfter, content.size(), totalElements, hasNext);
  }

  // 파워 유저 조회

  // 인기 리뷰 조회
}
