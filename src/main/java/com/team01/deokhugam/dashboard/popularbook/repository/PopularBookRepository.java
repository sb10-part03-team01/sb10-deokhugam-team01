package com.team01.deokhugam.dashboard.popularbook.repository;

import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository
    extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {}
