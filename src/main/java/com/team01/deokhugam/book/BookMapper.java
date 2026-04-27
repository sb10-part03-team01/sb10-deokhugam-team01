package com.team01.deokhugam.book;

import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.storage.ThumbnailStorage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {ThumbnailStorage.class})
public interface BookMapper {
  @Mapping(source = "thumbnailUrl", target = "thumbnailUrl", qualifiedByName = "toPresignedUrl")
  @Mapping(source = "rating", target = "rating", qualifiedByName = "roundRating")
  BookDto toDto(Book book);

  @Named("roundRating")
  default double roundRating(double rating) {
    return Math.round(rating * 10) / 10.0;
  }
}
