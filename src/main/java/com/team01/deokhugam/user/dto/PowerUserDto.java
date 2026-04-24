package com.team01.deokhugam.user.dto;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    DashboardPeriod period,
    OffsetDateTime createdAt,
    long rank,
    double score,
    double reviewScoreSum,
    long likeCount,
    long commentCount
) {

}
