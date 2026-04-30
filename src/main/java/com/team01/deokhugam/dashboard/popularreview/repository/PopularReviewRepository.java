package com.team01.deokhugam.dashboard.popularreview.repository;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import java.time.LocalDate;
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
}
