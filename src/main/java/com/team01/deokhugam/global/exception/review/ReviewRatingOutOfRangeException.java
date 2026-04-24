package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class ReviewRatingOutOfRangeException extends DeokhugamException {

  public ReviewRatingOutOfRangeException(double rating) {
    super(
        ErrorCode.REVIEW_RATING_OUT_OF_RANGE,
        Map.of(
            "rating", rating,
            "min", 1.0,
            "max", 5.0,
            "rule", "평점은 1.0 이상 5.0 이하여야 합니다."
        )
    );
  }
}
