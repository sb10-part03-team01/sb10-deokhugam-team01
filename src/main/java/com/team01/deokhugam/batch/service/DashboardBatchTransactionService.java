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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // 기존 랭킹 삭제
    powerUserRepository.deleteByPeriod(period);

    // 새 랭킹 저장
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

    List<UUID> bookIds = rows.stream().map(PopularBookScoreRow::bookId).toList();

    Map<UUID, Book> bookMap =
        bookRepository.findAllById(bookIds).stream().collect(Collectors.toMap(Book::getId, b -> b));

    // 같은 기간 + 같은 calculatedDate로 재실행될 수 있으므로 해당 스냅샷만 지운다.
    popularBookRepository.deleteByPeriodTypeAndCalculatedDate(dashboardPeriod, calculatedDate);

    List<PopularBook> rankings = new ArrayList<>();
    int rank = 1;

    for (PopularBookScoreRow row : rows) {
      Book book = bookMap.get(row.bookId());

      // 집계 대상 책이 실제 DB에서 누락된 경우는 스킵하되 로그로 남긴다.
      if (book == null) {
        log.warn(
            "[DASHBOARD_BATCH] 인기 도서 저장 대상 book 누락. dashboardPeriod={}, bookId={}",
            dashboardPeriod,
            row.bookId());
        continue;
      }

      rankings.add(
          new PopularBook(
              book,
              dashboardPeriod,
              calculatedDate,
              rank,
              row.score(),
              row.averageRating(),
              (int) row.reviewCount()));

      rank++;
    }

    popularBookRepository.saveAll(rankings);
  }
}
