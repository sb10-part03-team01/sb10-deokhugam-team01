package com.team01.deokhugam.review.repository;

import com.team01.deokhugam.review.dto.ReviewSearchCondition;
import com.team01.deokhugam.review.entity.Review;
import java.util.List;


public interface ReviewRepositoryCustom {

  List<Review> findAllByCondition(ReviewSearchCondition condition);

  long countByCondition(ReviewSearchCondition condition);
}
