package com.team01.deokhugam.global.exception.user;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserAccessDeniedException extends UserException {

  public UserAccessDeniedException(UUID pathUserId, UUID requestUserId) {
    super(
        ErrorCode.USER_ACCESS_DENIED,
        Map.of(
            "resourceId", pathUserId.toString(), // 접근하려는 대상의 리소스 소유자 ID
            "requestUserId", requestUserId.toString(), // 실제 요청을 보낸 사용자 ID
            "operation", "VERIFY_SELF"
        )
    );
  }
}
