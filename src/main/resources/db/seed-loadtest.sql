-- =====================================================================
-- 덕후감 LOAD TEST 시드 데이터
-- ---------------------------------------------------------------------
-- 위치    : src/main/resources/db/seed-loadtest.sql
-- 프로파일: loadtest 에서만 로드 (dev / qa / prod 미적용)
-- 분량    : User 10K / Book 50K / Review ~580K / Comment 1M / ReviewLike ~1.9M / Notification 200K
-- 시간대  : UTC (seed.base_date 기준일 핀, qa 시드와 동일 패턴)
-- 패스워드: 모두 'deokhugam1!' (BCrypt 동일 해시)
--           -> 부하 테스트 시 어떤 계정으로 로그인해도 동일 비번 사용 가능
-- 분포    : 책/유저 편중 분포 (현실 데이터 시뮬레이션)
--           - 책: 강한 파레토 — 인기 책 5K(10%)이 리뷰의 80% 보유
--           - 유저: 약한 편중   — 파워 유저 500명(5%)이 리뷰의 50% 작성
-- 미시드  : popular_books / popular_reviews / power_users
--           -> 위 3개 테이블은 배치 잡(스케줄러)이 만들어내는 결과물이므로
--             부하 테스트 환경에서 배치 자체를 검증하기 위해 비워둠
-- 예상시간: 로컬 PostgreSQL 14+ 기준 10~30분
--           (random(), gen_random_uuid() 호출이 전체 시간의 대부분 차지)
-- =====================================================================


-- 매번 깨끗한 상태에서 시작 (재현성 보장)
-- TRUNCATE 옵션 설명:
--   - RESTART IDENTITY : SERIAL/IDENTITY 시퀀스를 1로 리셋 (UUID라 직접 영향은 없지만 관례)
--   - CASCADE          : FK 로 연결된 자식 테이블도 함께 비움
--                        (review_likes → reviews → books/users 같은 의존 그래프)
-- DELETE 가 아닌 TRUNCATE 를 쓰는 이유:
--   - 수백만 건 DELETE 는 WAL(Write-Ahead Logging) 폭증 + 트리거 발동으로 매우 느림
--   - TRUNCATE 는 메타데이터만 갱신하므로 ms 단위로 끝남
-- WAL: 데이터베이스에서 모든 변경 사항을 실제 데이터 파일에 반영하기 전, 로그 파일에 먼저 기록하는 기술
TRUNCATE users, books, reviews, comments, review_likes, notifications,
         popular_books, popular_reviews, power_users
    RESTART IDENTITY CASCADE;


-- 기준일 핀 (qa 시드와 동일 패턴)
-- set_config(name, value, is_local):
--   - is_local = false -> 세션 전체에 적용 (이번 스크립트 끝까지 유지)
--   - is_local = true  -> 현재 트랜잭션에만 적용
SELECT set_config('seed.base_date',
                  (((CURRENT_TIMESTAMP AT TIME ZONE 'UTC'):: date) - 1)::text, false);

-- =====================================================================
-- 1) USERS (10,000명)
--    가입일은 g 값 기반으로 1년 내 결정론적 분산
-- =====================================================================
-- 핵심 로직:
--   ts = (기준일 - (g % 365)) + ((g % 1440)분)
--   -> g 가 1~10000 까지 돌면서 365일 × 1440분 격자에 결정론적으로 적재
--   -> random() 을 안 쓰므로 동일 g 면 동일 ts (재현 가능)
-- AT TIME ZONE 'UTC':
--   timestamp(타임존 없음) → timestamptz(UTC) 변환
--   PostgreSQL 의 timestamptz 컬럼은 내부적으로 UTC 로 저장됨
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
SELECT gen_random_uuid(), -- pgcrypto / PG13+ 내장: UUIDv4 / 매 행마다 다른 UUID -> PK 충돌 없음
       'loadtest_user_' || g || '@deokhugam.test', -- email 은 UNIQUE 제약 -> g 로 충돌 방지
       'user' || g,
       '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
       -- BCrypt('deokhugam1!') 의 고정 해시
       -- -> 매번 BCrypt 호출하면 1만 번 × 100ms = 매우 느림
       -- -> 같은 평문/해시를 박아 시드 시간 단축
       FALSE, -- soft delete 플래그
       ts,
       ts -- created_at = updated_at (신규 가입 시점)
FROM (SELECT g,
             ((current_setting('seed.base_date')::date - (g % 365))::timestamp
    + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
    --   ↑ (g % 365): 0~364일 전 사이 어떤 날짜로 분산
    --   ↑ (g % 1440): 0~1439분 사이 (= 하루 24h × 60m) 시각으로 분산
    --   결과적으로 1년치 가입자가 분/일 단위로 골고루 퍼짐
FROM generate_series(1, 10000) g -- PostgreSQL 내장 함수로 1부터 10000까지 정수 행을 생성
    ) t;

-- =====================================================================
-- 2) BOOKS (50,000권)
--    review_count, rating 은 0으로 INSERT -> 마지막에 UPDATE
-- =====================================================================
-- (review_count, rating) 을 0 으로 두는 이유:
--   - 리뷰 INSERT 때마다 books 의 카운트를 갱신하면
--     50K × 평균 12리뷰 = 60만 번 UPDATE -> 매우 느림
--   - 모든 데이터 INSERT 후 9번 단계에서 단일 GROUP BY 로 일괄 갱신
--     (set 기반 처리 -> 행 단위 처리보다 압도적으로 빠름)
INSERT INTO books (id, title, author, description, publisher, published_date,
                   isbn, thumbnail_url, review_count, rating,
                   is_deleted, created_at, updated_at)
SELECT gen_random_uuid(),
       'Load Test Book ' || g,
       'Author ' || (g % 1000), -- 1000명의 작가 풀에서 순환 (책 50,000권 / 작가 1,000명 -> 평균 50권)
       'Description for book ' || g,
       'Publisher ' || (g % 100), -- 100개 출판사 풀
       DATE '2015-01-01' + ((g % 4000) * INTERVAL '1 day'),
       -- 2015-01-01 ~ 약 2025-12 사이로 분산
       -- 정수에 '1일'을 곱함 -> N일이라는 기간이 됨
    LPAD(g::text, 13, '0'), -- ISBN: '0000000000001' ~ '0000000050000'
       -- LPAD(Left PAD)는 문자열의 왼쪽을 특정 문자로 채워서 고정 길이로 만들어주는 함수
       -- LPAD(원본문자열, 목표길이, 채울문자)
    NULL, -- thumbnail_url 은 부하 테스트에선 무의미 -> NULL
    0, 0.0,
    FALSE, ts, ts
FROM (
    SELECT
    g, ((current_setting('seed.base_date'):: date - (g % 365)):: timestamp
    + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
    FROM generate_series(1, 50000) g
    ) t;


-- =====================================================================
-- 3) 임시 lookup (미리 계산되어 저장된 결과 값을 모아둔 데이터 표)
--    row_number 부여 + 인덱스 -> 외래키 매핑을 O(1) 로
-- =====================================================================
-- 왜 임시 테이블이 필요한가?
--   리뷰/댓글/좋아요 삽입 시 '랜덤한 user/book 의 UUID FK' 가 필요하지만,
--   UUID 는 무작위라 'N 번째 행의 UUID' 를 직접 집을 수 없음.
--   -> rn(일련번호) 컬럼을 붙인 lookup 테이블을 만들면
--     `1 + floor(random() * 10000)` 같은 정수로 특정 UUID 를 바로 가져올 수 있음.
--
-- TEMP TABLE:
--   세션 종료 시 자동 삭제, WAL 미기록 -> 일반 테이블보다 빠름
--   이 트랜잭션/세션 안에서만 보임
--
-- ANALYZE:
--   PostgreSQL은 옵티마이저가 테이블별로 통계 정보 캐시를 들고 있다 / COUNT(*)를 매번 돌리지 않음
--     SELECT * FROM tmp_users u
--     JOIN tmp_books b ON u.rn = b.rn
--     WHERE u.rn < 100;
--   => 이런 쿼리가 있다고 가정하면 옵티마이저는 Nested Loop와 Hash join 중 더 비용이 낮은 것을 선택함
--   통계를 갱신해 옵티마이저가 실제 행 수를 인식하게 함.
--   없으면 이후 JOIN 계획이 잘못 수립되어 nested loop 폭주 가능 (대용량 데이터는 Hash Join이 효율적이기 때문)
CREATE TEMP TABLE tmp_users AS
SELECT
    id,
    row_number() OVER (ORDER BY created_at, id) AS rn
    -- id 도 정렬키에 포함 -> tie-breaker / 같은 ts 라도 결정론적 순서 보장
FROM users;
-- => users 테이블을 복사해서 각 행에 1, 2, 3, ... 일련번호(rn)를 붙인 임시 테이블 생성

CREATE UNIQUE INDEX ON tmp_users(rn); -- rn 으로 lookup / UNIQUE 명시로 옵티마이저 힌트
-- => 위에서 만든 임시 테이블의 rn 컬럼에 인덱스 생성(나중에 WHERE rn = 특정값로 조회할 때 속도 높이기 위함)
ANALYZE tmp_users; -- 임시 테이블의 통계 정보 수집

CREATE TEMP TABLE tmp_books AS
SELECT
    id, row_number() OVER (ORDER BY created_at, id) AS rn
FROM books;
CREATE UNIQUE INDEX ON tmp_books(rn);
ANALYZE tmp_books;
-- => 도서에 대해서도 동일한 작업 진행

-- =====================================================================
-- 4) REVIEWS (600K 시도 -> uk_reviews_book_user 충돌 SKIP -> 실제 ~580K)
--    편중 분포 (리뷰가 어느 그룹에 얼마나 쌓이는지):
--      - book: 리뷰의 80%는 인기 책(rn 1~5000, 전체의 10%) / 20%는 일반(rn 5001~50000, 90%) <- 강한 파레토(80/20)
--      - user: 리뷰의 50%는 파워 유저(rn 1~500, 전체의 5%) / 50%는 일반(rn 501~10000, 95%) <- 약한 편중(파레토 아님)
--    평점 분포: 4-5점 60% / 3-4점 25% / 1-3점 15%
-- =====================================================================
-- 왜 600K 시도 → 580K 결과인가?
--   uk_reviews_book_user (book_id, user_id) UNIQUE 제약 때문에
--   '같은 유저가 같은 책에 리뷰 2번' 은 불가능.
--   편중 분포 + 랜덤 매핑 시 어쩔 수 없이 (book, user) 쌍이 겹침.
--   -> ON CONFLICT DO NOTHING 으로 중복은 조용히 SKIP.
--   -> 시도 600K, 실제 적재 ~580K (약 3% 충돌 손실).
--
INSERT INTO reviews (id, book_id, user_id, content, rating,
                     like_count, comment_count,
                     is_deleted, created_at, updated_at)
-- CTE(Common Table Expression, 공통 테이블 표현식) 를 2단계로 나눈 이유:
--   1단계 params : random() 을 컬럼으로 고정 → CTE 가 한 번만 평가되도록 안정화
--   2단계 mapped : 고정된 random 값으로 rn / rating 계산
--   바깥 SELECT  : tmp_books, tmp_users 와 JOIN 해 실제 UUID 획득
-- CTE: 쿼리 안에서만 쓸 임시 결과 테이블에 이름을 붙이는 문법 / WITH ... AS (...) 형태
--      서브쿼리를 변수처럼 이름 붙여 재사용할 수 있게 해주는 도구
--   같은 행에서 일관된 random 값을 사용하려면 random()을 한 번만 호출해서 그 값을 컬럼에 고정 저장해야 한다.
WITH params AS (
    SELECT
        g, -- p_*: 편중 그룹 결정용 / r_*: 그룹 내 위치용
        random() AS p_book,
        random() AS r_book,
        random() AS p_user,
        random() AS r_user,
        random() AS p_rating,
        random() AS r_rating_minor,
        ((current_setting('seed.base_date')::date - (g % 365)) ::timestamp
            + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
    FROM generate_series(1, 600000) g
),
mapped AS (
    SELECT
        g, ts,
        -- 책 매핑 (강한 파레토: 인기 책 10% -> 리뷰 80%)
        CASE WHEN p_book < 0.8
            THEN 1 + floor(r_book * 5000):: int            -- 인기 책: rn 1~5000 (전체 책의 10%)
            ELSE 5001 + floor(r_book * 45000):: int        -- 일반 책: rn 5001~50000 (90%)
        END AS book_rn,
        -- 결과: 인기 책 5K 가 리뷰의 80% 받음 -> "허리띠가 두꺼운" 분포

        -- 유저 매핑 (약한 편중: 파워 유저 5% -> 리뷰 50%, 파레토 아님)
        CASE WHEN p_user < 0.5
            THEN 1 + floor(r_user * 500):: int             -- 파워 유저: rn 1~500 (5%)
            ELSE 501 + floor(r_user * 9500):: int          -- 일반 유저: rn 501~10000 (95%)
        END AS user_rn,
        -- 결과: 파워 유저 500명이 리뷰의 50% 작성

        -- 평점 분포 (현실의 리뷰 사이트는 보통 고평점 편향)
        CASE
            WHEN p_rating < 0.60 THEN 4.0 + r_rating_minor       -- 4.0~5.0 (60%)
            WHEN p_rating < 0.85 THEN 3.0 + r_rating_minor       -- 3.0~4.0 (25%)
            ELSE                      1.0 + r_rating_minor * 2.0 -- 1.0~3.0 (15%)
        END AS rating_value
    FROM params
)
SELECT
    gen_random_uuid(),
    b.id,
    u.id,
    'Load test review content ' || m.g,
    m.rating_value,
    0, -- like_count    : 9번 단계에서 일괄 UPDATE
    0, -- comment_count : 9번 단계에서 일괄 UPDATE
    FALSE,
    m.ts,
    m.ts
FROM mapped m
    JOIN tmp_books b ON b.rn = m.book_rn
    JOIN tmp_users u ON u.rn = m.user_rn
ON CONFLICT (book_id, user_id) DO NOTHING; -- 중복 쌍은 SKIP (DO UPDATE 없이 단순 무시)

-- =====================================================================
-- 5) reviews lookup (comments / likes / notifications 매핑용)
-- =====================================================================
CREATE TEMP TABLE tmp_reviews AS
SELECT
    id,
    row_number() OVER (ORDER BY created_at, id) AS rn
FROM reviews;
CREATE UNIQUE INDEX ON tmp_reviews(rn);
ANALYZE tmp_reviews;

-- =====================================================================
-- 6) COMMENTS (1,000,000)
-- 균등 분포를 쓰는 이유:
--   댓글은 토론성 행위라 인기 리뷰에만 몰리는 편향이 약함.
--   부하 테스트에서 다양한 review_id 에 고르게 부하를 주기 위해 균등 분포 사용.
--
-- (review_id, user_id) UNIQUE 제약이 없으므로 ON CONFLICT 불필요.
INSERT INTO comments (id, review_id, user_id, content, is_deleted, created_at, updated_at)
-- r_stat:
--   tmp_reviews 의 행 수를 한 번만 계산해 재사용
--   100만 번 COUNT(*) 호출하면 비용이 크므로 CTE 로 캐시
WITH r_stat AS (
    SELECT COUNT(*)::int AS c FROM tmp_reviews
)
SELECT
    gen_random_uuid(),
    r.id,
    u.id,
    'Load test comment ' || t.g,
    FALSE,
    t.ts,
    t.ts
FROM (
     SELECT
        g,
        1 + floor(random() * (SELECT c FROM r_stat))::int AS review_rn, -- 1~580000 균등
        1 + floor(random() * 10000)::int                  AS user_rn,   -- 1~10000 균등
        ((current_setting('seed.base_date')::date - (g % 365))::timestamp
            + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
     FROM generate_series(1, 1000000) g
) t
    JOIN tmp_reviews r ON r.rn = t.review_rn
    JOIN tmp_users u ON u.rn = t.user_rn;
-- 댓글은 (review_id, user_id) UNIQUE 가 없으므로 ON CONFLICT 불필요

-- =====================================================================
-- 7) REVIEW_LIKES (2.2M 시도 -> uk_review_like_review_user 충돌 SKIP -> 실제 ~1.9M)
-- 파레토 분포:
--   review : 인기 리뷰 상위 10% (rn 1~58000)   가 좋아요의 80% 수신
--   user   : 파워 유저 상위  5% (rn 1~500)     가 좋아요의 50% 누름
--
-- 충돌률 ~14% 인 이유:
--   파레토로 인기 리뷰/파워 유저에 집중 -> (review_id, user_id) 쌍 중복 확률 상승
--   ON CONFLICT DO NOTHING 으로 SKIP -> 2.2M 시도 / ~1.9M 적재
INSERT INTO review_likes (id, review_id, user_id, created_at, updated_at)
WITH r_stat AS (
    SELECT COUNT(*)::int AS c FROM tmp_reviews -- 행 수를 한 번만 계산해 캐시
),
params AS (
    SELECT
        g,
        random() AS p_review, -- 인기/일반 리뷰 그룹 결정
        random() AS r_review, -- 그룹 내 세부 위치
        random() AS p_user,   -- 파워/일반 유저 그룹 결정
        random() AS r_user,   -- 그룹 내 세부 위치
        ((current_setting('seed.base_date')::date - (g % 365)) ::timestamp
            + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
    FROM generate_series(1, 2200000) g
),
mapped AS (
    SELECT
        g, ts,
        -- 리뷰 매핑 (파레토 80/20)
        -- 인기 리뷰 : rn ∈ [1,       N*0.1] -> 80% 확률
        -- 일반 리뷰 : rn ∈ [N*0.1+1, N]     -> 20% 확률
        CASE WHEN p_review < 0.8
            THEN 1 + floor(r_review * (SELECT c FROM r_stat) * 0.1):: int
            ELSE 1 + floor((SELECT c FROM r_stat) * 0.1):: int
                   + floor(r_review * (SELECT c FROM r_stat) * 0.9):: int
        END AS review_rn,

        -- 유저 매핑 (50/50)
        CASE WHEN p_user < 0.5
            THEN 1 + floor(r_user * 500):: int    -- 파워 유저
            ELSE 501 + floor(r_user * 9500):: int -- 일반 유저
        END AS user_rn
    FROM params
)
SELECT
    gen_random_uuid(),
    r.id,
    u.id,
    m.ts,
    m.ts
FROM mapped m
    JOIN tmp_reviews r ON r.rn = m.review_rn
    JOIN tmp_users u ON u.rn = m.user_rn
ON CONFLICT (review_id, user_id) DO NOTHING; -- 같은 유저가 같은 리뷰에 좋아요 2번 불가

-- =====================================================================
-- 8) NOTIFICATIONS (200,000)
--    좋아요/댓글 알림 시뮬레이션 (최근 30일만)
--    is_read: 30% true (읽음), 70% false (안읽음)
-- =====================================================================
-- 최근 30일치만 생성하는 이유:
--   알림은 단기성 데이터라 부하 테스트의 페이징/안읽음 카운트 쿼리가
--   최근 데이터에 집중됨 -> g % 30 으로 0~29일 전 분포만 생성
--
-- is_read / confirmed_at 일관성:
--   is_read = true  -> confirmed_at = ts   (읽은 시각 기록)
--   is_read = false -> confirmed_at = NULL (미읽음)
INSERT INTO notifications (id, review_id, user_id, content, is_read, confirmed_at,
                           created_at, updated_at)
WITH r_stat AS (
    SELECT COUNT(*)::int AS c FROM tmp_reviews
)
SELECT gen_random_uuid(),
       r.id,
       u.id,
       'Load test notification ' || t.g,
       t.is_read_flag,
       CASE WHEN t.is_read_flag THEN t.ts ELSE NULL END, -- 읽음이면 확인 시각, 아니면 NULL
       t.ts,
       t.ts
FROM (
    SELECT
        g,
        1 + floor(random() * (SELECT c FROM r_stat))::int AS review_rn, -- 균등 분포
        1 + floor(random() * 10000)::int                  AS user_rn, -- 균등 분포
        random() < 0.3                                    AS is_read_flag, -- 30% 확률로 읽음
        ((current_setting('seed.base_date')::date - (g % 30))::timestamp
            + ((g % 1440) * INTERVAL '1 minute')) AT TIME ZONE 'UTC' AS ts
        -- 다른 테이블의 g % 365와 달리 30일치만 생성
    FROM generate_series(1, 200000) g
) t
    JOIN tmp_reviews r ON r.rn = t.review_rn
    JOIN tmp_users   u ON u.rn = t.user_rn;

-- =====================================================================
-- 9) 캐시 컬럼 일괄 UPDATE
-- =====================================================================
-- 1~8단계에서 캐시 컬럼(review_count, like_count 등)은 모두 0으로 삽입됨.
-- 마지막에 GROUP BY 집계 후 UPDATE ... FROM 으로 한 번에 갱신.
-- -> 행 단위 트리거보다 훨씬 빠르고, 정렬/랭킹 쿼리가 올바른 값을 반환하게 됨.

-- 9-1) books: 리뷰 개수 + 평균 평점
UPDATE books b
SET review_count = sub.cnt,
    rating       = sub.avg_rating
FROM (
    SELECT book_id, COUNT(*) AS cnt, AVG(rating) AS avg_rating
    FROM reviews
    WHERE is_deleted = false -- soft delete 된 리뷰는 집계 제외
    GROUP BY book_id
) sub
WHERE b.id = sub.book_id;
-- 리뷰가 0개인 책은 sub 에 포함되지 않아 UPDATE 대상에서 제외됨
-- -> INSERT 시 이미 0으로 초기화되어 있으므로 문제 없음

-- 9-2) reviews: 좋아요 수 갱신
UPDATE reviews r
SET like_count = sub.cnt
FROM (
    SELECT review_id, COUNT(*) AS cnt
    FROM review_likes
    GROUP BY review_id
) sub
WHERE r.id = sub.review_id;

-- 9-3) reviews: 댓글 수 갱신
UPDATE reviews r
SET comment_count = sub.cnt
FROM (
    SELECT review_id, COUNT(*) AS cnt
    FROM comments
    WHERE is_deleted = false -- soft delete 된 댓글 제외
    GROUP BY review_id
) sub
WHERE r.id = sub.review_id;

-- =====================================================================
-- 10) 임시 테이블 정리
-- =====================================================================
-- 세션 종료 시 자동 삭제되지만 명시적으로 DROP:
--   - 같은 세션에서 다른 작업이 이어질 경우 메모리/디스크 회수
DROP TABLE IF EXISTS tmp_users;
DROP TABLE IF EXISTS tmp_books;
DROP TABLE IF EXISTS tmp_reviews;

-- =====================================================================
-- 11) 검증 쿼리 (필요시 주석 해제)
-- =====================================================================
-- 테이블별 행 수 확인 (예상치와 비교)
-- SELECT 'users' tbl, COUNT(*) FROM users
-- UNION ALL SELECT 'books',         COUNT(*) FROM books
-- UNION ALL SELECT 'reviews',       COUNT(*) FROM reviews
-- UNION ALL SELECT 'comments',      COUNT(*) FROM comments
-- UNION ALL SELECT 'review_likes',  COUNT(*) FROM review_likes
-- UNION ALL SELECT 'notifications', COUNT(*) FROM notifications;

-- 인기 책 검증 (리뷰 많은 순) - 상위 10권은 모두 인기 책 그룹(rn 1~5000) 이어야 함
-- SELECT title, review_count, rating FROM books ORDER BY review_count DESC LIMIT 10;

-- 파워 유저 검증 (리뷰 많이 쓴 순) - 상위는 모두 user1 ~ user500 이어야 함
-- SELECT u.nickname, COUNT(r.id) AS review_cnt
-- FROM users u JOIN reviews r ON r.user_id = u.id
-- GROUP BY u.id, u.nickname
-- ORDER BY review_cnt DESC LIMIT 10;
