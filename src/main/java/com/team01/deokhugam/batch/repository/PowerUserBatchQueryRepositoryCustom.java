package com.team01.deokhugam.batch.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.UserActivityCountRow;
import com.team01.deokhugam.batch.dto.UserReviewScoreSumRow;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface PowerUserBatchQueryRepositoryCustom {

  List<UserReviewScoreSumRow> findReviewScoreSumsByUser(
      DashboardPeriod period,
      LocalDate calculatedDate
  );

  List<UserActivityCountRow> findLikeCountsByUserBetween(
      OffsetDateTime start,
      OffsetDateTime end
  );
}
