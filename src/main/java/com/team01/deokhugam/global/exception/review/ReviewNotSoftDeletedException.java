package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class ReviewNotSoftDeletedException extends DeokhugamException {

  public ReviewNotSoftDeletedException() {
    super(
        ErrorCode.REVIEW_NOT_SOFT_DELETED,
        Map.of(
            "rule", "논리 삭제된 리뷰만 물리 삭제할 수 있습니다."
        )
    );
  }
}
