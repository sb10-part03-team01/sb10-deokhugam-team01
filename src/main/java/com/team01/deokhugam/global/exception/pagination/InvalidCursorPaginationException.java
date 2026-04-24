package com.team01.deokhugam.global.exception.pagination;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class InvalidCursorPaginationException extends DeokhugamException {

  public InvalidCursorPaginationException() {
    super(
        ErrorCode.INVALID_CURSOR_PAGINATION,
        Map.of(
            "rule", "cursor와 after는 함께 제공되어야 합니다."
        )
    );
  }
}
