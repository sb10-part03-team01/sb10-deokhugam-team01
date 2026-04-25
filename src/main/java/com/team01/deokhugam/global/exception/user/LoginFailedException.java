package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class LoginFailedException extends UserException {

  public LoginFailedException() {
    super(
        ErrorCode.LOGIN_FAILED,
        Map.of(
            "operation", "LOGIN"
        )
    );
  }
}
