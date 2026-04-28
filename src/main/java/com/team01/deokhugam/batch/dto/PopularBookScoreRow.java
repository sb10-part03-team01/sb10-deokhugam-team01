package com.team01.deokhugam.batch.dto;

import java.util.UUID;

public record PopularBookScoreRow(
    UUID bookId, long reviewCount, double averageRating, double score) {}
