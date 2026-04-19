package com.team01.deokhugam.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // book

  BOOK_NOT_FOUND(404, "BOOK_NOT_FOUND", "도서를 찾을 수 없습니다"),
  DUPLICATED_ISBN(409, "DUPLICATED_ISBN", "중복된 ISBN이 존재합니다."),
  INVALID_CURSOR_FORMAT(400, "INVALID_CURSOR_FORMAT", "cursor 형식이 올바르지 않습니다."),
  INVALID_CURSOR_PAGINATION(400, "INVALID_CURSOR_PAGINATION", "cursor와 after는 함께 제공되어야 합니다."),

  // comment

  // notification

  // ranking

  // review

  // user
  EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS", "이미 등록된 이메일입니다."),
  USER_NOT_FOUND(404, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  LOGIN_FAILED(401, "LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다.");

  private final int status;
  private final String code;
  private final String message;
}
