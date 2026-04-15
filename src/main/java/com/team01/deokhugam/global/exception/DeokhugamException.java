package com.team01.deokhugam.global.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class DeokhugamException extends RuntimeException{
  private final ErrorCode errorCode;
  private final Map<String, Object> details;
  private final Instant timestamp;

  public DeokhugamException(ErrorCode errorCode, Map<String, Object> details){
    this.errorCode = errorCode;
    this.details = details;
    this.timestamp = Instant.now();
  }
}
