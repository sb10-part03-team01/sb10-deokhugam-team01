package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class EmailAlreadyExistsException extends UserException {

  public EmailAlreadyExistsException() {
    super(
        ErrorCode.EMAIL_ALREADY_EXISTS,
        Map.of(
            "operation", "REGISTER",
            "rule", "동일 이메일의 사용자가 존재할 수 없음"
        )
    );
  }
}
