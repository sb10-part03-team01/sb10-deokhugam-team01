package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class EmailAlreadyExistsException extends UserException {

  public EmailAlreadyExistsException(String email) {
    super(
        ErrorCode.EMAIL_ALREADY_EXISTS,
        Map.of(
            "email", email,
            "operation", "REGISTER",
            "rule", "이메일은 중복될 수 없음"
        )
    );
  }
}
