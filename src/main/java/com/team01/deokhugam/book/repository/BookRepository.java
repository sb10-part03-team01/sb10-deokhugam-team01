package com.team01.deokhugam.book.repository;

import com.team01.deokhugam.book.entity.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryQueryDsl {
  boolean existsByIsbnAndIsDeletedFalse(String isbn);

  Optional<Book> findByIdAndIsDeletedFalse(UUID id);

  Optional<Book> findByIdAndIsDeletedTrue(UUID id);
}
