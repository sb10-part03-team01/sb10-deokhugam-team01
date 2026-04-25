package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException(UUID userId) {
    super(
        ErrorCode.USER_NOT_FOUND,
        Map.of(
            "resourceId", userId.toString(),
            "operation", "FIND_BY_ID",
            "currentState", "NOT_FOUND_IN_DB"
        )
    );
  }
}
