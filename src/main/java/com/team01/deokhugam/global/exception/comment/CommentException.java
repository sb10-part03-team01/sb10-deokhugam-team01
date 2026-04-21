package com.team01.deokhugam.global.exception.comment;

import com.team01.deokhugam.global.exception.DeokhugamException;
import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class CommentException extends DeokhugamException {
  public CommentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
