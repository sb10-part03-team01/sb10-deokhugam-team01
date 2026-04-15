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
    log.warn(" [Domain Error] Code: {}, Message: {}, Details: {}",
        e.getErrorCode().getCode(),
        e.getErrorCode().getMessage(),
        e.getDetails());
    ErrorResponse response = new ErrorResponse(
        e.getTimeStamp(),
        e.getErrorCode().getCode(),
        e.getErrorCode().getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        e.getErrorCode().getStatus()
    );
    return ResponseEntity.status(response.getStatus()).body(response);
  }

}
