package com.team01.deokhugam.dashboard.popularreview.mapper;

import com.team01.deokhugam.book.storage.ThumbnailStorage;
import com.team01.deokhugam.dashboard.popularreview.dto.PopularReviewDto;
import com.team01.deokhugam.dashboard.popularreview.entity.PopularReview;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ThumbnailStorage.class})
public interface PopularReviewMapper {

  @Mapping(target = "id", source = "id")
  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "bookId", source = "review.book.id")
  @Mapping(target = "bookTitle", source = "review.book.title")
  @Mapping(
      target = "bookThumbnailUrl",
      source = "review.book.thumbnailUrl",
      qualifiedByName = "toPresignedUrl"
  )
  @Mapping(target = "userId", source = "review.user.id")
  @Mapping(target = "userNickname", source = "review.user.nickname")
  @Mapping(target = "reviewContent", source = "review.content")
  @Mapping(target = "reviewRating", source = "review.rating")
  @Mapping(target = "period", source = "period")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "rank", source = "rank")
  @Mapping(target = "score", source = "score")
  @Mapping(target = "likeCount", source = "likeCount")
  @Mapping(target = "commentCount", source = "commentCount")
  PopularReviewDto toDto(PopularReview popularReview);

  List<PopularReviewDto> toDtoList(List<PopularReview> popularReviews);
}
