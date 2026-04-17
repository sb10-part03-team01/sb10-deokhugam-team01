package com.team01.deokhugam.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  BOOK_NOT_FOUND(404, "BOOK_NOT_FOUND", "도서를 찾을 수 없습니다"),
  DUPLICATED_ISBN(409, "DUPLICATED_ISBN", "중복된 ISBN이 존재합니다."),
  COMMENT_NOT_FOUND(404, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
  REVIEW_NOT_FOUND(404, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
  USER_NOT_FOUND(404, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  FORBIDDEN_COMMENT_ACCESS(403, "FORBIDDEN_COMMENT_ACCESS", "댓글에 대한 권한이 없습니다."),
  INVALID_CURSOR_FORMAT(400, "INVALID_CURSOR_FORMAT", "cursor 형식이 올바르지 않습니다."),
  INVALID_CURSOR_PAGINATION(400, "INVALID_CURSOR_PAGINATION", "cursor와 after는 함께 제공되어야 합니다.");

  private final int status;
  private final String code;
  private final String message;
}
