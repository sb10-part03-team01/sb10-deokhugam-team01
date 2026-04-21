package com.team01.deokhugam.global.exception.comment;

import com.team01.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {
  public CommentNotFoundException(UUID commentId) {
    super(
        ErrorCode.COMMENT_NOT_FOUND,
        Map.of(
            "resourceId", commentId.toString(),
            "operation", "FIND_COMMENT",
            "currentState", "NOT_FOUND",
            "rule", "댓글은 존재해야 합니다"));
  }
}
