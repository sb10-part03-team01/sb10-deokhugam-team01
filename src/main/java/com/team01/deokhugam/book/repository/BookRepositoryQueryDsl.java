package com.team01.deokhugam.book.repository;

import com.team01.deokhugam.book.Book;
import java.time.OffsetDateTime;
import java.util.List;

public interface BookRepositoryQueryDsl {
  List<Book> findBooks(String keyword, String orderBy, String direction, String cursor, OffsetDateTime after, int limit);

  long countBooks(String keyword);


}
