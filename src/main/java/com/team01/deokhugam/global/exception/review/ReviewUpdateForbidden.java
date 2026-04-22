package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewUpdateForbidden extends DeokhugamException {

  public ReviewUpdateForbidden(UUID reviewId, UUID requestUserId) {
    super(
        ErrorCode.REVIEW_UPDATE_FORBIDDEN,
        Map.of(
            "requestUserId",
            requestUserId.toString(),
            "reviewId",
            reviewId.toString(),
            "rule",
            "권한이 있는 사용자만 리뷰를 수정할 수 있음"
        )
    );
  }
}
