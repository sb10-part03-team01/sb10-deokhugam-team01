package com.team01.deokhugam.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 1, max = 1000, message = "리뷰는 1~1000자 사이여야 합니다.")
    String content,

    @NotNull(message = "평점은 필수입니다.")
    @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    Double rating
) {

}