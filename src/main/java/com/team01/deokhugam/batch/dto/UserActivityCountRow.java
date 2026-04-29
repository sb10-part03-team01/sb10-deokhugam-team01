package com.team01.deokhugam.batch.dto;

import java.util.UUID;

public record UserActivityCountRow(
    UUID userId,
    long count
) {

}
