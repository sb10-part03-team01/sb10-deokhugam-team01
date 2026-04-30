package com.team01.deokhugam.batch.dto;

import java.util.UUID;

public record UserReviewScoreSumRow(
    UUID userId,
    double scoreSum
) {

}
