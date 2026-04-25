package com.team01.deokhugam.global.pagination;

import java.time.OffsetDateTime;

public record CursorPageRequest(
    String cursor,
    OffsetDateTime after,
    Integer limit
) {

}
