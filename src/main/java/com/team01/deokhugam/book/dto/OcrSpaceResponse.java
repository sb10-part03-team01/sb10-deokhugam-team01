package com.team01.deokhugam.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record OcrSpaceResponse(
    @JsonProperty("ParsedResults")
    List<ParsedResult> parsedResults,
    @JsonProperty("IsErroredOnProcessing")
    boolean isErroredOnProcessing
    ) {
  public record ParsedResult(
      @JsonProperty("ParsedText")
      String parsedText,
      @JsonProperty("ErrorMessage")
      String errorMessage
  ) {

  }
}
