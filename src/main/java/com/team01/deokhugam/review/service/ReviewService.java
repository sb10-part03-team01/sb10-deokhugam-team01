package com.team01.deokhugam.review.service;

import com.team01.deokhugam.review.dto.ReviewCreateRequest;
import com.team01.deokhugam.review.dto.ReviewDto;

public interface ReviewService {

  ReviewDto createReview(ReviewCreateRequest request);

}
