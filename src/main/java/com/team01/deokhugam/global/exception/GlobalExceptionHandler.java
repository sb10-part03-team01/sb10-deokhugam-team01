package com.team01.deokhugam.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(DeokhugamException.class)
  public ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e){
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

}
