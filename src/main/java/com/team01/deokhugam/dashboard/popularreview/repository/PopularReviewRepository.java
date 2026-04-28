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

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

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
          r.id,
          coalesce(count(distinct rl.id), 0),
          coalesce(count(distinct c.id), 0)
      )
      from Review r
      left join ReviewLike rl
          on rl.review = r
         and rl.createdAt >= :start
         and rl.createdAt < :end
      left join Comment c
          on c.review = r
         and c.createdAt >= :start
         and c.createdAt < :end
         and c.isDeleted = false
      where r.isDeleted = false
      group by r.id
      having count(distinct rl.id) > 0
          or count(distinct c.id) > 0
      """)
  List<PopularReviewScoreRow> findPopularReviewScoreRows(
      @Param("start") OffsetDateTime start,
      @Param("end") OffsetDateTime end
  );

}
