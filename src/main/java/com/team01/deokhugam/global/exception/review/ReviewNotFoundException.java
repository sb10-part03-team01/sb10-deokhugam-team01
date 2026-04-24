package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends DeokhugamException {

  public ReviewNotFoundException(UUID reviewId) {
    super(
        ErrorCode.REVIEW_NOT_FOUND,
        Map.of(
            "reviewId",
            reviewId.toString(),
            "rule",
            "리뷰를 찾을 수 없습니다."
        )
    );
  }
}
