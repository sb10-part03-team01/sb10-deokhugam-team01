package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotSoftDeletedException extends UserException {

  public UserNotSoftDeletedException(UUID userId) {
    super(
        ErrorCode.USER_NOT_SOFT_DELETED,
        Map.of(
            "resourceId", userId.toString(),
            "operation", "HARD_DELETE",
            "currentState", "NOT_SOFT_DELETED"
        )
    );
  }
}
