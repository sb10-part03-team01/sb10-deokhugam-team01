package com.team01.deokhugam.global.exception.review;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewAlreadyExistsException extends DeokhugamException {

  public ReviewAlreadyExistsException(UUID bookId, UUID userId) {
    super(
        ErrorCode.REVIEW_ALREADY_EXISTS,
        Map.of(
            "bookId", bookId.toString(),
            "userId", userId.toString(),
            "rule", "해당 도서에 작성한 리뷰가 있습니다"
        )
    );
  }
}
