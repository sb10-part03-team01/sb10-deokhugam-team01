package com.team01.deokhugam.book;

import com.team01.deokhugam.book.dto.BookDto;
import com.team01.deokhugam.book.entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
  BookDto toDto(Book book);
}
