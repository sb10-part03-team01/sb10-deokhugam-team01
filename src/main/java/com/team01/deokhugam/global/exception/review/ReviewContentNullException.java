package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class ReviewContentNullException extends DeokhugamException {

  public ReviewContentNullException() {
    super(
        ErrorCode.REVIEW_CONTENT_NULL,
        Map.of("rule", "리뷰 내용은 NULL일 수 없습니다.")
    );
  }
}
