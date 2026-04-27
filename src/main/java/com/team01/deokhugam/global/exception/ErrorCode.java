package com.team01.deokhugam.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // book
  INVALID_BOOK_SORT_FIELD(400, "INVALID_BOOK_SORT_FIELD", "알맞은 정렬 기준이 아닙니다."),
  ISBN_UNIDENTIFIABLE(400,"ISBN_UNIDENTIFIABLE","ISBN을 식별 할 수 없습니다."),
  INVALID_FILE(400, "INVALID_FILE","유효하지 않은 파일입니다"),
  EMPTY_FILE_UPLOADED(400, "EMPTY_FILE_UPLOADED", "비어있는 파일입니다."),
  BOOK_NOT_FOUND(404, "BOOK_NOT_FOUND", "해당하는 도서를 찾을 수 없습니다"),
  NAVER_BOOK_NOT_FOUND(404, "NAVER_BOOK_NOT_FOUND", "해당 ISBN의 도서를 찾을 수 없습니다"),
  DUPLICATED_ISBN(409, "DUPLICATED_ISBN", "중복된 ISBN이 존재합니다."),
  INVALID_CURSOR_FORMAT(400, "INVALID_CURSOR_FORMAT", "cursor 형식이 올바르지 않습니다."),
  INVALID_CURSOR_PAGINATION(400, "INVALID_CURSOR_PAGINATION", "cursor와 after는 함께 제공되어야 합니다."),
  FILE_SIZE_EXCEEDED(413, "FILE_SIZE_EXCEEDED", "제한된 파일 크기를 초과했습니다."),
  UNSUPPORTED_FILE_FORMAT(415, "UNSUPPORTED_FILE_FORMAT", "지원하지 않는 파일 형식입니다."),
  THUMBNAIL_UPLOAD_FAIL(500, "THUMBNAIL_UPLOAD_FAIL", "썸네일 업로드에 실패했습니다"),
  API_CREDENTIAL_FAIL(500, "API_CREDENTIAL_FAIL", "해당 API 자격 증명에 실패했습니다"),
  API_SERVER_ERROR(502 ,"API_SERVER_ERROR", "API 서버 오류가 발생했습니다"),

  // comment
  COMMENT_NOT_FOUND(404, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
  FORBIDDEN_COMMENT_ACCESS(403, "FORBIDDEN_COMMENT_ACCESS", "댓글에 대한 권한이 없습니다."),

  // notification
  NOTIFICATION_NOT_FOUND(404, "NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다."),
  NOTIFICATION_ACCESS_DENIED(403, "NOTIFICATION_ACCESS_DENIED", "알림 수정 권한이 없습니다."),

  // ranking

  // review
  REVIEW_NOT_FOUND(404, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
  REVIEW_UPDATE_FORBIDDEN(403, "REVIEW_UPDATE_FORBIDDEN", "리뷰를 수정할 권한이 없습니다."),
  REVIEW_CONTENT_NULL(400, "REVIEW_CONTENT_NULL", "리뷰 내용은 null일 수 없습니다."),
  REVIEW_CONTENT_BLANK(400, "REVIEW_CONTENT_BLANK", "리뷰 내용은 비어 있을 수 없습니다."),
  REVIEW_CONTENT_TOO_LONG(400, "REVIEW_CONTENT_TOO_LONG", "리뷰는 1000자를 초과할 수 없습니다."),
  REVIEW_RATING_OUT_OF_RANGE(400, "REVIEW_RATING_OUT_OF_RANGE", "평점은 1.0 이상 5.0 이하여야 합니다."),
  REVIEW_ALREADY_EXISTS(409, "REVIEW_ALREADY_EXISTS", "해당 도서에 작성한 리뷰가 있습니다."),
  REVIEW_NOT_SOFT_DELETED(400, "REVIEW_NOT_SOFT_DELETED", "논리 삭제된 리뷰만 물리 삭제할 수 있습니다."),
  // user
  EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS", "이미 등록된 이메일입니다."),
  USER_NOT_FOUND(404, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  LOGIN_FAILED(401, "LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다."),
  USER_ACCESS_DENIED(403, "USER_ACCESS_DENIED", "해당 사용자에 대한 권한이 없습니다."),
  MISSING_REQUEST_USER_ID(400, "MISSING_REQUEST_USER_ID", "요청자 ID가 누락되었습니다."),
  USER_NOT_SOFT_DELETED(404, "USER_NOT_SOFT_DELETED", "논리 삭제되지 않은 사용자는 물리 삭제할 수 없습니다.");

  private final int status;
  private final String code;
  private final String message;
}
