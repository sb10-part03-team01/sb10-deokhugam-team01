package com.team01.deokhugam.global.exception.book;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BookNotFoundException extends BookException {

  public BookNotFoundException(UUID bookId) {
    super(ErrorCode.BOOK_NOT_FOUND, Map.of(
        "resourceId", bookId.toString(),
        "operation", "FIND_BY_ID",
        "currentState", "NOT_FOUND_IN_DB",
        "rule", "유효한 UUID에 해당하는 도서가 존재해야 함"
    ));
  }
}
