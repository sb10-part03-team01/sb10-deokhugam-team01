package com.team01.deokhugam.book;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {
  boolean existsByIsbn(String isbn);

  Optional<Book> findByIdAndIsDeletedFalse(UUID id);

  Page<Book> findAllByIsDeletedFalse(Pageable pageable);

}
