package com.team01.deokhugam.global.exception.book;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class BookException extends DeokhugamException {

  public BookException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
