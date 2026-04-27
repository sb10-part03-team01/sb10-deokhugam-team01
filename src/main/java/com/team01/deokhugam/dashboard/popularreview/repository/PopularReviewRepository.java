package com.team01.deokhugam.dashboard.popularreview.repository;


import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  void deleteByPeriod(DashboardPeriod period);

  List<PopularReview> findAllByPeriodOrderByRankAsc(DashboardPeriod period);
}
