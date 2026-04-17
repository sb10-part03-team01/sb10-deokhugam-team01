package com.team01.deokhugam.global.exception.book;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class DuplicatedIsbnException extends BookException {

  public DuplicatedIsbnException(String isbn) {
    super(ErrorCode.DUPLICATED_ISBN, Map.of(
        "resourceId", isbn,
        "operation", "CREATE_BOOK",
        "currentState", "DUPLICATED_ISBN",
        "rule", "중복된 ISBN이 DB에 존재할 수 없음"
    ));
  }
}
