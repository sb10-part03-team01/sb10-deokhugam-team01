package com.team01.deokhugam.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OcrSpaceResponse(
    @JsonProperty("ParsedResults")
    List<ParsedResult> parsedResults,
    @JsonProperty("IsErroredOnProcessing")
    boolean isErroredOnProcessing
    ) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ParsedResult(
      @JsonProperty("ParsedText")
      String parsedText,
      @JsonProperty("ErrorMessage")
      String errorMessage
  ) {

  }
}
