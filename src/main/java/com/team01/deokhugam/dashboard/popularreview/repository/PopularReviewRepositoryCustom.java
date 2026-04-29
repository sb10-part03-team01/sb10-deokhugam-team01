package com.team01.deokhugam.dashboard.popularreview.repository;

import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewSearchCondition;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import java.util.List;

public interface PopularReviewRepositoryCustom {

  List<PopularReview> findAllByCondition(PopularReviewSearchCondition condition);

  long countByCondition(PopularReviewSearchCondition condition);

}
