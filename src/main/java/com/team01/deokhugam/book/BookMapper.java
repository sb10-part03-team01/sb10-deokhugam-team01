package com.team01.deokhugam.book;

import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.storage.ThumbnailStorage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ThumbnailStorage.class})
public interface BookMapper {
  @Mapping(source = "thumbnailUrl", target = "thumbnailUrl", qualifiedByName = "toPresignedUrl")
  BookDto toDto(Book book);
}
