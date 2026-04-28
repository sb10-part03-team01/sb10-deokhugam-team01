package com.team01.deokhugam.batch.repository;

import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import java.time.OffsetDateTime;
import java.util.List;

public interface PopularBookBatchQueryRepositoryCustom {
  List<PopularBookScoreRow> findPopularBooksBetween(OffsetDateTime start, OffsetDateTime end);
}
