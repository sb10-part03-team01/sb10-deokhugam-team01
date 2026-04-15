package com.team01.deokhugam.global.exception;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  private Instant timeStamp;
  private String code;
  private String message;
  private Map<String, Object> details;
  private String exceptionType;
  private int status;
}
