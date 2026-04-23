package com.team01.deokhugam.poweruser.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import com.team01.deokhugam.global.pagination.CursorPaginationUtils;
import com.team01.deokhugam.global.pagination.PageLimitPolicy;
import com.team01.deokhugam.poweruser.entity.PowerUser;
import com.team01.deokhugam.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.user.dto.PowerUserDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserServiceImpl implements PowerUserService {

  private final PowerUserRepository powerUserRepository;

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<PowerUserDto> getRanking(DashboardPeriod period, SortDirection direction,
      String cursor, OffsetDateTime after, int limit) {

    List<PowerUser> results;

    int normalizedLimit = PageLimitPolicy.normalize(limit);
    PageRequest pageable = PageRequest.of(0, normalizedLimit + 1);
    boolean asc = direction == SortDirection.ASC;

    if (after == null) {
      if (cursor != null && !cursor.isBlank()) {
        throw new DeokhugamException(ErrorCode.INVALID_CURSOR_PAGINATION, Map.of("cursor", cursor));
      }
      results = asc
          ? powerUserRepository.findByPeriodOrderByRankAsc(period, pageable)
          : powerUserRepository.findByPeriodOrderByRankDesc(period, pageable);
    } else {
      if (cursor == null || cursor.isBlank()) {
        throw new DeokhugamException(ErrorCode.INVALID_CURSOR_PAGINATION, Map.of("after", after));
      }
      long rankCursor;
      try {
        rankCursor = Long.parseLong(cursor);
      } catch (NumberFormatException e) {
        throw new DeokhugamException(ErrorCode.INVALID_CURSOR_FORMAT, Map.of("cursor", cursor));
      }
      results = asc
          ? powerUserRepository.findByPeriodAndRankGreaterThanAndCreatedAtAfterOrderByRankAscCreatedAtAsc(period, rankCursor, after, pageable)
          : powerUserRepository.findByPeriodAndRankLessThanAndCreatedAtBeforeOrderByRankDescCreatedAtDesc(period, rankCursor, after, pageable);
    }

    List<PowerUserDto> dtoList = results.stream()
        .map(pu -> new PowerUserDto(
            pu.getUser().getId(),
            pu.getUser().getNickname(),
            pu.getPeriod(),
            pu.getCreatedAt(),
            pu.getRank(),
            pu.getScore(),
            pu.getReviewScoreSum(),
            pu.getLikeCount(),
            pu.getCommentCount()
        )).toList();

    long totalElements = powerUserRepository.countByPeriod(period);

    return CursorPaginationUtils.of(
        dtoList,
        normalizedLimit,
        totalElements,
        dto -> String.valueOf(dto.rank()),
        dto -> dto.createdAt()
    );
  }
}
