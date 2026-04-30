package com.team01.deokhugam.dashboard.popularreview.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>,
    PopularReviewRepositoryCustom {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      delete from PopularReview pr
      where pr.period = :period
        and pr.calculatedDate = :calculatedDate
      """)
  int deleteByPeriodAndCalculatedDate(
      @Param("period") DashboardPeriod period,
      @Param("calculatedDate") LocalDate calculatedDate
  );

  @EntityGraph(attributePaths = {"review", "review.book", "review.user"})
  List<PopularReview> findAllByPeriodAndCalculatedDateOrderByRankAsc(
      DashboardPeriod period,
      LocalDate calculatedDate
  );

  @Query("""
      select new com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow(
          rl.review.id,
          count(rl.id),
          0L
      )
      from ReviewLike rl
      where rl.createdAt >= :start
        and rl.createdAt < :end
        and rl.review.isDeleted = false
      group by rl.review.id
      """)
  List<PopularReviewScoreRow> findPopularReviewLikeScoreRows(
      @Param("start") OffsetDateTime start,
      @Param("end") OffsetDateTime end
  );

  @Query("""
      select new com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewScoreRow(
          c.review.id,
          0L,
          count(c.id)
      )
      from Comment c
      where c.createdAt >= :start
        and c.createdAt < :end
        and c.isDeleted = false
        and c.review.isDeleted = false
      group by c.review.id
      """)
  List<PopularReviewScoreRow> findPopularReviewCommentScoreRows(
      @Param("start") OffsetDateTime start,
      @Param("end") OffsetDateTime end
  );
}
