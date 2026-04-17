package com.team01.deokhugam.review.mapper;

import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.entity.Review;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {OffsetDateTime.class, ZoneOffset.class})
public interface ReviewMapper {

  // Review 엔티티를 ReviewDto로 변환
  @Mapping(target = "bookId", source = "book.id")
  @Mapping(target = "bookTitle", source = "book.title")
  @Mapping(target = "bookThumbnailUrl", source = "book.thumbnailUrl")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userNickname", source = "user.nickname")
  @Mapping(target = "likedByMe", ignore = true)
  @Mapping(target = "createdAt", expression = "java(OffsetDateTime.ofInstant(review.getCreatedAt(), ZoneOffset.UTC))")
  @Mapping(target = "updatedAt", expression = "java(OffsetDateTime.ofInstant(review.getUpdatedAt(), ZoneOffset.UTC))")
  ReviewDto toDto(Review review);

  // 리스트 변환
  List<ReviewDto> toDtoList(List<Review> reviews);
}