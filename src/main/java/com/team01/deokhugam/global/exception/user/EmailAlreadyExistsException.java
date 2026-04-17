package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class EmailAlreadyExistsException extends UserException {

  public EmailAlreadyExistsException(String email) {
    super(
        ErrorCode.EMAIL_ALREADY_EXISTS,
        Map.of("operation", "REGISTER")
    );
  }
}
