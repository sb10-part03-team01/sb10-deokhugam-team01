package com.team01.deokhugam.global.exception;

import com.team01.deokhugam.global.constant.AuthHeader;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(DeokhugamException.class)
  public ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e) {
    ErrorCode errorCode = e.getErrorCode();

    log.warn(" [Domain Error] Code: {}, Message: {}, Details: {}",
        errorCode.getCode(),
        errorCode.getMessage(),
        e.getDetails());

    ErrorResponse response = new ErrorResponse(
        e.getTimeStamp(),
        errorCode.getCode(),
        errorCode.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        errorCode.getStatus()
    );
    return ResponseEntity.status(response.getStatus()).body(response);
  }

  // ServletException 처리 - 체크드 예외 (Checked Exception)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e) {
    // REQUEST_USER_ID 누락
    if (AuthHeader.REQUEST_USER_ID.equals(e.getHeaderName())) {
      ErrorCode errorCode = ErrorCode.MISSING_REQUEST_USER_ID;

      log.warn(" [Missing Header] Code: {}, Message: {}, Header: {}",
          errorCode.getCode(),
          errorCode.getMessage(),
          e.getHeaderName());

      ErrorResponse response = new ErrorResponse(
          Instant.now(),
          errorCode.getCode(),
          errorCode.getMessage(),
          Map.of("headerName", e.getHeaderName()),
          e.getClass().getSimpleName(),
          errorCode.getStatus()
      );
      return ResponseEntity.status(response.getStatus()).body(response);
    }

    // 그 외 헤더 누락 -> 일반 400
    log.warn(" [Missing Header] {}", e.getMessage());

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        "MISSING_REQUEST_HEADER",
        "필수 헤더가 누락되었습니다.",
        Map.of("headerName", e.getHeaderName()),
        e.getClass().getSimpleName(),
        400
    );
    return ResponseEntity.badRequest().body(response);
  }

}
