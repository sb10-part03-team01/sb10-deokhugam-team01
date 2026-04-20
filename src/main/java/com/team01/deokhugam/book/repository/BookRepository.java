package com.team01.deokhugam.book.repository;

import com.team01.deokhugam.book.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryQueryDsl {
  boolean existsByIsbn(String isbn);

  Optional<Book> findByIdAndIsDeletedFalse(UUID id);

}
