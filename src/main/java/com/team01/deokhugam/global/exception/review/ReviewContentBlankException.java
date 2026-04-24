package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class ReviewContentBlankException extends DeokhugamException {

  public ReviewContentBlankException() {
    super(
        ErrorCode.REVIEW_CONTENT_BLANK,
        Map.of("rule", "리뷰 내용은 비어 있을 수 없습니다.")
    );
  }
}
