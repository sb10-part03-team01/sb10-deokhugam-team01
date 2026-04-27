package com.team01.deokhugam.batch.service;

import com.team01.deokhugam.batch.common.DashboardPeriod;
import com.team01.deokhugam.batch.dto.PopularBookScoreRow;
import com.team01.deokhugam.book.entity.Book;
import com.team01.deokhugam.book.repository.BookRepository;
import com.team01.deokhugam.dashboard.popularbook.entity.PopularBook;
import com.team01.deokhugam.dashboard.popularbook.repository.PopularBookRepository;
import com.team01.deokhugam.dashboard.poweruser.entity.PowerUser;
import com.team01.deokhugam.dashboard.poweruser.repository.PowerUserRepository;
import com.team01.deokhugam.global.exception.user.UserNotFoundException;
import com.team01.deokhugam.user.entity.User;
import com.team01.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardBatchTransactionService {

  private final PowerUserRepository powerUserRepository;
  private final UserRepository userRepository;
  private final PopularBookRepository popularBookRepository;
  private final BookRepository bookRepository;

  @Transactional
  public void deleteAndSave(
      DashboardPeriod period, List<Map.Entry<UUID, Double>> rank, OffsetDateTime calculatedAt) {

    List<UUID> userIds = rank.stream().map(Map.Entry::getKey).toList();

    Map<UUID, User> mapUser =
        userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    if (mapUser.size() != userIds.size()) {
      UUID missing =
          userIds.stream().filter(id -> !mapUser.containsKey(id)).findFirst().orElseThrow();
      throw new UserNotFoundException(missing);
    }

    //  기존 삭제
    powerUserRepository.deleteByPeriod(period);

    //  저장
    List<PowerUser> rankings = new ArrayList<>();
    for (int i = 0; i < rank.size(); i++) {
      UUID userId = rank.get(i).getKey();
      User user = mapUser.get(userId);
      if (user == null) {
        throw new UserNotFoundException(userId);
      }
      rankings.add(
          PowerUser.builder()
              .user(user)
              .period(period)
              .calculatedDate(calculatedAt)
              .rank(i + 1)
              .score(rank.get(i).getValue())
              .reviewScoreSum(0.0) // TODO
              .likeCount(0L) // TODO
              .commentCount(0L) // TODO
              .build());
    }
    powerUserRepository.saveAll(rankings);
  }

  @Transactional
  public void deleteAndSavePopularBooks(
      DashboardPeriod dashboardPeriod, List<PopularBookScoreRow> rows, LocalDate calculatedDate) {

    DashboardPeriod rankingPeriod = toRankingPeriod(dashboardPeriod);

    List<UUID> bookIds = rows.stream().map(PopularBookScoreRow::bookId).toList();

    Map<UUID, Book> bookMap =
        bookRepository.findAllById(bookIds).stream().collect(Collectors.toMap(Book::getId, b -> b));

    // 같은 기간 + 같은 calculatedDate로 재실행될 수 있으므로 해당 스냅샷만 지운다.
    popularBookRepository.deleteByPeriodTypeAndCalculatedDate(rankingPeriod, calculatedDate);

    List<PopularBook> rankings = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
      PopularBookScoreRow row = rows.get(i);
      Book book = bookMap.get(row.bookId());

      if (book == null) {
        continue;
      }

      rankings.add(
          new PopularBook(
              book,
              rankingPeriod,
              calculatedDate,
              i + 1,
              row.score(),
              row.averageRating(),
              (int) row.reviewCount()));
    }

    popularBookRepository.saveAll(rankings);
  }

  private DashboardPeriod toRankingPeriod(DashboardPeriod dashboardPeriod) {
    return switch (dashboardPeriod) {
      case DAILY -> DashboardPeriod.DAILY;
      case WEEKLY -> DashboardPeriod.WEEKLY;
      case MONTHLY -> DashboardPeriod.MONTHLY;
      case ALL_TIME -> DashboardPeriod.ALL_TIME;
    };
  }
}
