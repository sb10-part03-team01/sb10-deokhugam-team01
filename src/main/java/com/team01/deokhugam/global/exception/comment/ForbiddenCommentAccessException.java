package com.team01.deokhugam.global.exception.comment;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ForbiddenCommentAccessException extends CommentException {
  public ForbiddenCommentAccessException(UUID userId, UUID commentId) {
    super(
        ErrorCode.FORBIDDEN_COMMENT_ACCESS,
        Map.of(
            "userId",
            userId.toString(),
            "commentId",
            commentId.toString(),
            "operation",
            "UPDATE_OR_DELETE_COMMENT",
            "rule",
            "댓글 작성자만 수정/삭제할 수 있음"));
  }
}
