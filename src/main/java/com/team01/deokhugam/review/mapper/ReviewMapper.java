package com.team01.deokhugam.review.mapper;

import com.team01.deokhugam.review.dto.ReviewDto;
import com.team01.deokhugam.review.entity.Review;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mapping(target = "bookTitle", ignore = true)
  @Mapping(target = "bookThumbnailUrl", ignore = true)
  @Mapping(target = "userNickname", ignore = true)
  @Mapping(target = "likedByMe", ignore = true)
  @Mapping(target = "updatedAt", source = "updatedAt")
  ReviewDto toDto(Review review);

  List<ReviewDto> toDtoList(List<Review> reviews);

}
