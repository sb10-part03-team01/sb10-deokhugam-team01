package com.team01.deokhugam.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum  ErrorCode {
  BOOK_NOT_FOUND(404, "BOOK_NOT_FOUND", "도서를 찾을 수 없습니다"),
  DUPLICATED_ISBN(409, "DUPLICATED_ISBN","중복된 ISBN이 존재합니다.");

  private final int status;
  private final String code;
  private final String message;
}
