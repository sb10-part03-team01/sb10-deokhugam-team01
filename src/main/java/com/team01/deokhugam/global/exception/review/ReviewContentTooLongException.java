package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class ReviewContentTooLongException extends DeokhugamException {

  public ReviewContentTooLongException(int maxLength) {
    super(
        ErrorCode.REVIEW_CONTENT_TOO_LONG,
        Map.of(
            "maxLength", maxLength,
            "rule", "리뷰는 1000자를 초과할 수 없습니다."
        )
    );
  }
}
