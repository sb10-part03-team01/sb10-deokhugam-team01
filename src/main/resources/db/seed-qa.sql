-- ===========================================================================
-- 덕후감 QA 시드 데이터
-- 위치      : src/main/resources/db/seed-qa.sql  (qa 프로파일에서만 로드)
-- 기준일    : 배치 baseDate 의 D-1 (= UTC 기준 어제). seed.base_date = CURRENT_DATE - 1
--            배치는 UTC 자정에 baseDate=오늘(UTC)로 실행되며 DAILY 윈도우는
--            [baseDate-1 00:00, baseDate 00:00) 이므로 시드의 '기준일' 데이터가 DAILY 에 잡힘.
-- 시간 분산  : DAILY(기준일 당일) / WEEKLY(기준일 -2~-6일) / MONTHLY(기준일 -8~-25일) / ALL_TIME(기준일 -60일+)
-- 시간대    : 모든 timestamp 는 UTC 기준 (AT TIME ZONE 'UTC'). 배치/도메인 전반 UTC 통일 합의에 맞춤.
-- 분량      : User 8 / Book 13(12 + ISBN 중복 검증용 1) / Review 24 /
--            Comment 36 / ReviewLike 36 / Notification 10
-- 비밀번호 : 모든 계정 raw="deokhugam1!" (BCrypt 동일 해시 사용)
-- ===========================================================================

-- 매 부팅마다 깨끗한 상태에서 시작 (validate 모드 + sql.init.mode:always 환경에서 PK 충돌 방지)
TRUNCATE TABLE
    notifications,
    review_likes,
    comments,
    reviews,
    books,
    users,
    popular_books,
    popular_reviews,
    power_users
RESTART IDENTITY CASCADE; -- 시퀀스(자동 증가 번호)도 리셋 / 외래키로 묶인 테이블도 같이 비움

-- ===========================================================================
-- 기준일 핀(pin)
-- 이 시드의 모든 timestamp 는 '기준일(seed.base_date) 기준 D-N일 전 HH:MM:SS UTC' 표현식으로 작성됨.
-- 자정 경계에서 statement 간 시점이 갈리는 것을 막기 위해 첫 statement에서 base_date 를
-- 세션 변수에 한 번 고정한 뒤, 이후 INSERT 는 모두 이 값을 참조한다.
-- base_date = CURRENT_DATE - 1 인 이유: 배치는 UTC 오늘 자정에 실행되며 DAILY 윈도우가
-- [어제 00:00 UTC, 오늘 00:00 UTC) 이므로 '어제(UTC)'가 기준일이어야 DAILY 데이터로 잡힌다.
-- 시드 적재 시점이 바뀌어도 DAILY/WEEKLY/MONTHLY/ALL_TIME 분류는 항상 동일하게 재현된다.
-- ===========================================================================
SELECT set_config('seed.base_date', (((CURRENT_TIMESTAMP AT TIME ZONE 'UTC')::date) - 1)::text, false);

-- ===========================================================================
-- 1) USERS (8명: 프로토타입 5 + 일반 2 + 논리삭제 1)
-- ===========================================================================
INSERT INTO users (id, email, nickname, password, is_deleted, deleted_at, created_at, updated_at) VALUES
-- 프로토타입 계정 (모두 활성)
('11111111-1111-1111-1111-100000000001', 'minjun.kim@deokhugam.test',  '김민준',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 177 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 177 + TIME '09:00:00') AT TIME ZONE 'UTC')),
('11111111-1111-1111-1111-100000000002', 'seoyeon.lee@deokhugam.test', '이서연',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 177 + TIME '09:10:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 177 + TIME '09:10:00') AT TIME ZONE 'UTC')),
('11111111-1111-1111-1111-100000000003', 'jihoon.park@deokhugam.test', '박지훈',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 177 + TIME '09:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 177 + TIME '09:20:00') AT TIME ZONE 'UTC')),
('11111111-1111-1111-1111-100000000004', 'yerin.choi@deokhugam.test',  '최예린',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 177 + TIME '09:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 177 + TIME '09:30:00') AT TIME ZONE 'UTC')),
('11111111-1111-1111-1111-100000000005', 'dohyun.jung@deokhugam.test', '정도현',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 177 + TIME '09:40:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 177 + TIME '09:40:00') AT TIME ZONE 'UTC')),
-- 일반 계정
('11111111-1111-1111-1111-100000000006', 'sua.kang@deokhugam.test',    '강수아',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 147 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 147 + TIME '10:00:00') AT TIME ZONE 'UTC')),
('11111111-1111-1111-1111-100000000007', 'jiwoo.yoon@deokhugam.test',  '윤지우',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 147 + TIME '10:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 147 + TIME '10:30:00') AT TIME ZONE 'UTC')),
-- 논리삭제 계정 (가입 후 탈퇴 처리, 검증용)
('11111111-1111-1111-1111-100000000008', 'taemin.han@deokhugam.test',  '한태민',
 '$2a$10$skmO41mqA8D3b4UWClBcueVC.65QMGLFzi.3woCIbC4fvCgj5SoSu',
 TRUE, ((current_setting('seed.base_date')::date - 2 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 26 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 2 + TIME '12:00:00') AT TIME ZONE 'UTC'));

-- ===========================================================================
-- 2) BOOKS (13권: 12 + ISBN 중복 검증용 1)
--    review_count / rating 은 활성 리뷰 기준으로 미리 계산해서 넣음
--    (논리삭제된 리뷰는 카운트에서 제외)
-- ===========================================================================
INSERT INTO books (id, title, author, description, publisher, published_date, isbn,
                   thumbnail_url, review_count, rating,
                   is_deleted, deleted_at, created_at, updated_at) VALUES
-- b01 : 인기 도서 (리뷰 3 / 평점 4.5)
('22222222-2222-2222-2222-200000000001',
 '어린 왕자', '앙투안 드 생텍쥐페리', '사막에 불시착한 비행사가 작은 별에서 온 어린 왕자를 만나며 풀어가는 사색의 우화.',
 '문학과지성사', '1943-04-06', '9788932037561', NULL, 3, 4.5,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 147 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 147 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- b02 : 인기 도서 (리뷰 3 / 평점 4.7)
('22222222-2222-2222-2222-200000000002',
 '위대한 개츠비', 'F. 스콧 피츠제럴드', '재즈 시대의 화려함 뒤에 가려진 한 남자의 사랑과 환상을 그려낸 미국 문학의 정수.',
 '민음사', '1925-04-10', '9788937460753', NULL, 3, 4.7,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 133 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 133 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- b03 : 평범 도서 (리뷰 2 / 평점 3.75)
('22222222-2222-2222-2222-200000000003',
 '호밀밭의 파수꾼', 'J.D. 샐린저', '학교에서 쫓겨난 소년 홀든의 사흘간을 통해 사회의 위선을 폭로한 청춘 소설.',
 '민음사', '1951-07-16', '9788937460470', NULL, 2, 3.75,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 116 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 116 + TIME '11:00:00') AT TIME ZONE 'UTC')),
-- b04 : 평점 낮음 (활성 리뷰 1 / 평점 3.0, 논리삭제 리뷰 1건 포함)
('22222222-2222-2222-2222-200000000004',
 '변신', '프란츠 카프카', '어느 날 아침 흉측한 벌레로 변해버린 그레고르와 가족 사이의 단절을 그린 부조리 문학의 고전.',
 '솔', '1915-10-15', '9791160200140', NULL, 1, 3.0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 128 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 128 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- b05 : (리뷰 2 / 평점 4.25)
('22222222-2222-2222-2222-200000000005',
 '노인과 바다', '어니스트 헤밍웨이', '거대한 청새치와의 사흘간의 사투를 통해 인간의 의지와 패배를 그린 짧은 서사시.',
 '새움', '1952-09-01', '9791190473507', NULL, 2, 4.25,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 102 + TIME '13:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 102 + TIME '13:00:00') AT TIME ZONE 'UTC')),
-- b06 : (리뷰 2 / 평점 3.5)
('22222222-2222-2222-2222-200000000006',
 '이방인', '알베르 카뮈', '어머니의 죽음 앞에서도 무덤덤한 한 남자의 권태와 부조리를 다룬 카뮈의 대표작.',
 '책세상', '1942-05-15', '9788970131092', NULL, 2, 3.5,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 97 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 97 + TIME '11:00:00') AT TIME ZONE 'UTC')),
-- b07 : (리뷰 2 / 평점 4.0)
('22222222-2222-2222-2222-200000000007',
 '데미안', '헤르만 헤세', '소년 싱클레어가 알을 깨고 자기 자신의 세계를 찾아가는 영혼의 성장 서사.',
 '민음사', '1919-06-15', '9788937460449', NULL, 2, 4.0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 92 + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 92 + TIME '14:00:00') AT TIME ZONE 'UTC')),
-- b08 : 평점 높음 (리뷰 2 / 평점 4.75)
('22222222-2222-2222-2222-200000000008',
 '죄와 벌', '표도르 도스토예프스키', '가난한 대학생 라스콜리니코프의 살인과 그 후의 정신적 갈등을 다룬 19세기 러시아 명작.',
 '열린책들', '1866-12-01', '9788932909158', NULL, 2, 4.75,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 85 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 85 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- b09 : (활성 리뷰 1 / 평점 3.5, 논리삭제 리뷰 1건 포함)
('22222222-2222-2222-2222-200000000009',
 '폭풍의 언덕', '에밀리 브론테', '황량한 요크셔 황무지에서 펼쳐지는 히스클리프와 캐서린의 격렬하고 비극적인 사랑.',
 '민음사', '1847-12-01', '9788937461187', NULL, 1, 3.5,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 107 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 107 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- b10 : (리뷰 2 / 평점 4.25)
('22222222-2222-2222-2222-200000000010',
 '1984', '조지 오웰', '빅브라더가 모든 것을 감시하는 디스토피아 사회에서 한 남자가 시도하는 마지막 저항.',
 '민음사', '1949-06-08', '9788937460777', NULL, 2, 4.25,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 102 + TIME '16:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 102 + TIME '16:00:00') AT TIME ZONE 'UTC')),
-- b11 : 비교적 최근 명작 (리뷰 2 / 평점 5.0, 등록일 최근)
('22222222-2222-2222-2222-200000000011',
 '채식주의자', '한강', '평범하던 한 여인이 어느 날 채식을 선언하며 균열되어가는 가족과 욕망의 이야기.',
 '창비', '2007-10-30', '9788936433598', NULL, 2, 5.0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 6 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 6 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- b12 : 리뷰 0건 (검색에는 잡히지만 정렬에서 후순위 검증용)
('22222222-2222-2222-2222-200000000012',
 '안나 카레니나', '레프 톨스토이', '19세기 러시아 사교계를 배경으로 한 여성의 사랑과 파멸을 그린 톨스토이의 대작.',
 '민음사', '1877-04-01', '9788937486074', NULL, 0, 0.0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 116 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 116 + TIME '12:00:00') AT TIME ZONE 'UTC')),
-- b13 : ISBN 중복 검증용 (활성 도서, isbn UNIQUE 제약 위반 시도용 더미)
('22222222-2222-2222-2222-200000000013',
 '오만과 편견', '제인 오스틴', '오해와 자존심을 거쳐 사랑에 이르는 베넷가 자매들의 결혼과 성장 이야기.',
 '열린책들', '1813-01-28', '9788932911434', NULL, 0, 0.0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 116 + TIME '12:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 116 + TIME '12:30:00') AT TIME ZONE 'UTC'));

-- ===========================================================================
-- 3) REVIEWS (24건)
--    시간 분산: DAILY 4 / WEEKLY 6 / MONTHLY 8 / ALL_TIME 6 (논리삭제 2건 포함)
--    유저당 3~4건 / 책당 1~3건 분산
-- ===========================================================================
INSERT INTO reviews (id, book_id, user_id, content, rating,
                     like_count, comment_count, version,
                     is_deleted, deleted_at, created_at, updated_at) VALUES

-- ---- DAILY (기준일 당일): 4건 ----
-- r01: u1(김민준) → b01, 기준일 09:00
('33333333-3333-3333-3333-300000000001', '22222222-2222-2222-2222-200000000001', '11111111-1111-1111-1111-100000000001',
 '오랜만에 다시 읽어도 여전히 좋은 책입니다.', 4.5, 5, 3, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- r02: u2(이서연) → b01, 기준일 10:30
('33333333-3333-3333-3333-300000000002', '22222222-2222-2222-2222-200000000001', '11111111-1111-1111-1111-100000000002',
 '여우와 어린 왕자의 길들임 장면이 어른이 되어 다시 읽으니 더 깊이 와닿았습니다.', 4.0, 4, 4, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '10:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:30:00') AT TIME ZONE 'UTC')),
-- r03: u3(박지훈) → b01, 기준일 14:00 [DAILY 인기리뷰 1위 후보]
('33333333-3333-3333-3333-300000000003', '22222222-2222-2222-2222-200000000001', '11111111-1111-1111-1111-100000000003',
 '한 챕터 한 챕터가 모두 인상 깊었습니다. 올해 최고의 책.', 5.0, 6, 6, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:00:00') AT TIME ZONE 'UTC')),
-- r04: u5(정도현) → b11(신간), 기준일 15:00
('33333333-3333-3333-3333-300000000004', '22222222-2222-2222-2222-200000000011', '11111111-1111-1111-1111-100000000005',
 '감각적이면서도 묵직한 문장이 끝까지 긴장을 놓지 못하게 만듭니다.', 5.0, 2, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC')),

-- ---- WEEKLY (최근 2~6일): 6건 ----
-- r05: u4(최예린) → b02, -3일
('33333333-3333-3333-3333-300000000005', '22222222-2222-2222-2222-200000000002', '11111111-1111-1111-1111-100000000004',
 '초록 불빛 너머의 환상이 어떤 의미였는지 마지막 장에서 다시 곱씹게 됩니다.', 4.5, 3, 3, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- r06: u5(정도현) → b02, -4일
('33333333-3333-3333-3333-300000000006', '22222222-2222-2222-2222-200000000002', '11111111-1111-1111-1111-100000000005',
 '한 번 읽고 또 읽고 싶은 책입니다.', 5.0, 2, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 4 + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 4 + TIME '14:00:00') AT TIME ZONE 'UTC')),
-- r07: u6(강수아) → b02, -5일
('33333333-3333-3333-3333-300000000007', '22222222-2222-2222-2222-200000000002', '11111111-1111-1111-1111-100000000006',
 '재즈 시대의 화려함과 그 이면의 공허가 잘 대비되어 그려진 작품입니다.', 4.5, 1, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 5 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 5 + TIME '11:00:00') AT TIME ZONE 'UTC')),
-- r08: u1(김민준) → b03, -3일
('33333333-3333-3333-3333-300000000008', '22222222-2222-2222-2222-200000000003', '11111111-1111-1111-1111-100000000001',
 '홀든의 시선으로 본 어른들의 위선이 시간이 지나도 여전히 날카롭게 느껴집니다.', 3.5, 2, 1, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '16:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '16:00:00') AT TIME ZONE 'UTC')),
-- r09: u7(윤지우) → b03, -2일
('33333333-3333-3333-3333-300000000009', '22222222-2222-2222-2222-200000000003', '11111111-1111-1111-1111-100000000007',
 '1인칭 화법 특유의 솔직함이 시대를 넘어 그대로 통하는 작품입니다.', 4.0, 1, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 2 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 2 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- r10: u1(김민준) → b11(신간), -6일
('33333333-3333-3333-3333-300000000010', '22222222-2222-2222-2222-200000000011', '11111111-1111-1111-1111-100000000001',
 '한강 작가 특유의 절제된 문장이 책장을 덮은 뒤에도 오래 남습니다.', 5.0, 1, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 6 + TIME '18:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 6 + TIME '18:00:00') AT TIME ZONE 'UTC')),

-- ---- MONTHLY (최근 8~25일): 8건 ----
-- r11: u3(박지훈) → b05, -15일
('33333333-3333-3333-3333-300000000011', '22222222-2222-2222-2222-200000000005', '11111111-1111-1111-1111-100000000003',
 '노인의 사흘간의 사투에서 인간의 존엄이라는 단어를 다시 생각하게 됐습니다.', 4.0, 3, 4, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 15 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- r12: u5(정도현) → b05, -12일
('33333333-3333-3333-3333-300000000012', '22222222-2222-2222-2222-200000000005', '11111111-1111-1111-1111-100000000005',
 '혼자 견디는 시간의 의미가 묵직하게 와닿았습니다.', 4.5, 1, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 12 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 12 + TIME '11:00:00') AT TIME ZONE 'UTC')),
-- r13: u2(이서연) → b08, -10일
('33333333-3333-3333-3333-300000000013', '22222222-2222-2222-2222-200000000008', '11111111-1111-1111-1111-100000000002',
 '라스콜리니코프의 내적 독백이 끝까지 긴장을 놓을 수 없게 만들었습니다.', 4.5, 2, 3, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 10 + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '14:00:00') AT TIME ZONE 'UTC')),
-- r14: u5(정도현) → b08, -8일
('33333333-3333-3333-3333-300000000014', '22222222-2222-2222-2222-200000000008', '11111111-1111-1111-1111-100000000005',
 '방대한 분량이지만 한 페이지도 덜어낼 곳 없는 명작입니다.', 5.0, 1, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 8 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 8 + TIME '09:00:00') AT TIME ZONE 'UTC')),
-- r15: u4(최예린) → b07, -18일
('33333333-3333-3333-3333-300000000015', '22222222-2222-2222-2222-200000000007', '11111111-1111-1111-1111-100000000004',
 '알을 깨고 나오는 새의 이미지가 오래도록 잔상으로 남습니다.', 4.0, 1, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 18 + TIME '13:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 18 + TIME '13:00:00') AT TIME ZONE 'UTC')),
-- r16: u1(김민준) → b07, -20일 (좋아요 0, 댓글 0)
('33333333-3333-3333-3333-300000000016', '22222222-2222-2222-2222-200000000007', '11111111-1111-1111-1111-100000000001',
 '성장기에 한 번쯤 통과해야 할 책이라는 생각이 듭니다.', 4.0, 0, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 20 + TIME '16:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 20 + TIME '16:00:00') AT TIME ZONE 'UTC')),
-- r17: u6(강수아) → b06, -22일
('33333333-3333-3333-3333-300000000017', '22222222-2222-2222-2222-200000000006', '11111111-1111-1111-1111-100000000006',
 '뫼르소의 무심함이 부조리라는 단어 그 자체로 다가옵니다.', 3.5, 1, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 22 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 22 + TIME '11:00:00') AT TIME ZONE 'UTC')),
-- r18: u7(윤지우) → b06, -25일 (좋아요 0, 댓글 0)
('33333333-3333-3333-3333-300000000018', '22222222-2222-2222-2222-200000000006', '11111111-1111-1111-1111-100000000007',
 '내용은 무난하나 후반부가 다소 늘어집니다.', 3.5, 0, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 25 + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 25 + TIME '14:00:00') AT TIME ZONE 'UTC')),

-- ---- ALL_TIME (60일+): 6건 (활성 4 + 논리삭제 2) ----
-- r19: u2(이서연) → b04, -65일
('33333333-3333-3333-3333-300000000019', '22222222-2222-2222-2222-200000000004', '11111111-1111-1111-1111-100000000002',
 '그레고르의 변신을 받아들이는 가족의 모습이 변신 자체보다 더 끔찍했습니다.', 3.0, 0, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 65 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 65 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- r20: u4(최예린) → b04, -90일 [논리삭제]
('33333333-3333-3333-3333-300000000020', '22222222-2222-2222-2222-200000000004', '11111111-1111-1111-1111-100000000004',
 '초반 몰입은 좋았지만 결말이 너무 답답해 평을 내립니다. (이후 본인 삭제)', 4.0, 0, 0, 0,
 TRUE, ((current_setting('seed.base_date')::date - 71 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 90 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 71 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- r21: u3(박지훈) → b09, -70일 [논리삭제]
('33333333-3333-3333-3333-300000000021', '22222222-2222-2222-2222-200000000009', '11111111-1111-1111-1111-100000000003',
 '히스클리프의 광기에 공감이 어려워 평을 내립니다. (이후 본인 삭제)', 2.0, 0, 0, 0,
 TRUE, ((current_setting('seed.base_date')::date - 57 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 70 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 57 + TIME '10:00:00') AT TIME ZONE 'UTC')),
-- r22: u6(강수아) → b09, -80일
('33333333-3333-3333-3333-300000000022', '22222222-2222-2222-2222-200000000009', '11111111-1111-1111-1111-100000000006',
 '황무지의 풍경 묘사가 인물의 감정과 하나로 이어지는 작품입니다.', 3.5, 0, 2, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 80 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 80 + TIME '12:00:00') AT TIME ZONE 'UTC')),
-- r23: u4(최예린) → b10, -75일
('33333333-3333-3333-3333-300000000023', '22222222-2222-2222-2222-200000000010', '11111111-1111-1111-1111-100000000004',
 '감시 사회에 대한 통찰이 출간 후 수십 년이 지나도 유효합니다.', 4.5, 0, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 75 + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 75 + TIME '15:00:00') AT TIME ZONE 'UTC')),
-- r24: u7(윤지우) → b10, -100일
('33333333-3333-3333-3333-300000000024', '22222222-2222-2222-2222-200000000010', '11111111-1111-1111-1111-100000000007',
 '여러 번 읽어도 발견하는 디테일이 있습니다.', 4.0, 0, 0, 0,
 FALSE, NULL, ((current_setting('seed.base_date')::date - 100 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 100 + TIME '11:00:00') AT TIME ZONE 'UTC'));

-- ===========================================================================
-- 4) COMMENTS (36건) - 리뷰별 0~6개 분포 (0개인 리뷰 다수 포함)
--    분포: r03=6, r02=4, r01=3, r05=3, r11=4, r13=3,
--          r06=2, r07=2, r14=2, r17=2, r19=2, r22=2,
--          r08=1, 그 외 0개 (r04/r09/r10/r12/r15/r16/r18/r20/r21/r23/r24)
-- ===========================================================================
INSERT INTO comments (id, review_id, user_id, content,
                      is_deleted, deleted_at, created_at, updated_at) VALUES

-- ---- r03 (인기 리뷰, 6개) ----
('44444444-4444-4444-4444-400000000001', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000001',
 '제대로 된 서평이네요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '14:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000002', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000002',
 '저도 이 책 인생 책에 추가했습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '14:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:45:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000003', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000004',
 '리뷰 보고 사러 갑니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000004', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000005',
 '구절구절이 인용하고 싶을 정도네요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '15:15:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:15:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000005', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000006',
 '오늘 본 리뷰 중 가장 좋네요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '15:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000006', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000007',
 '동감입니다. 추천 감사합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '16:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '16:00:00') AT TIME ZONE 'UTC')),

-- ---- r02 (4개) ----
('44444444-4444-4444-4444-400000000007', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000001',
 '여우와의 길들임 부분, 늘 마음을 울리네요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000008', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000003',
 '우정 부분 공감합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '11:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:20:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000009', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000004',
 '재독해야겠어요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '11:40:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:40:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000010', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000005',
 '추천 감사합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '12:00:00') AT TIME ZONE 'UTC')),

-- ---- r01 (3개) ----
('44444444-4444-4444-4444-400000000011', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000002',
 '저도 다시 읽고 싶어졌습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '09:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '09:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000012', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000003',
 '클래식의 힘이네요.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '09:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '09:45:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000013', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000006',
 '소장각.', FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:00:00') AT TIME ZONE 'UTC')),

-- ---- r05 (3개) ----
('44444444-4444-4444-4444-400000000014', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000001',
 '의외의 관점이 좋네요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '11:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000015', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000002',
 '초록 불빛에 대한 해석에 동의합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '11:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000016', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000005',
 '리뷰가 책보다 더 흥미로웠어요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '12:00:00') AT TIME ZONE 'UTC')),

-- ---- r11 (4개) ----
('44444444-4444-4444-4444-400000000017', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000001',
 '제목 그대로의 책이네요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 15 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '11:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000018', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000002',
 '리뷰 보고 한번 읽어봐야겠습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 15 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '11:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000019', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000005',
 '개인적으로 별 다섯입니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 15 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '12:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000020', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000006',
 '잘 읽었습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 15 + TIME '12:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '12:30:00') AT TIME ZONE 'UTC')),

-- ---- r13 (3개) ----
('44444444-4444-4444-4444-400000000021', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000004',
 '관점의 전환이 인상적이네요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 10 + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '15:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000022', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000005',
 '여러 번 읽고 싶은 리뷰입니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 10 + TIME '15:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '15:30:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000023', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000007',
 '내적 독백의 압도감, 정말 동감입니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 10 + TIME '16:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '16:00:00') AT TIME ZONE 'UTC')),

-- ---- r06 (2개) ----
('44444444-4444-4444-4444-400000000024', '33333333-3333-3333-3333-300000000006', '11111111-1111-1111-1111-100000000001',
 '재독 권하는 책 좋아합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 4 + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 4 + TIME '15:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000025', '33333333-3333-3333-3333-300000000006', '11111111-1111-1111-1111-100000000004',
 '동감.', FALSE, NULL, ((current_setting('seed.base_date')::date - 4 + TIME '15:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 4 + TIME '15:30:00') AT TIME ZONE 'UTC')),

-- ---- r07 (2개) ----
('44444444-4444-4444-4444-400000000026', '33333333-3333-3333-3333-300000000007', '11111111-1111-1111-1111-100000000004',
 '재즈 시대 묘사 부분에 깊이 동감합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 5 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 5 + TIME '12:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000027', '33333333-3333-3333-3333-300000000007', '11111111-1111-1111-1111-100000000007',
 '한 번 펼쳐봐야겠습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 5 + TIME '12:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 5 + TIME '12:30:00') AT TIME ZONE 'UTC')),

-- ---- r14 (2개) ----
('44444444-4444-4444-4444-400000000028', '33333333-3333-3333-3333-300000000014', '11111111-1111-1111-1111-100000000002',
 '장편이지만 술술 읽힌다는 평, 동감입니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 8 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 8 + TIME '10:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000029', '33333333-3333-3333-3333-300000000014', '11111111-1111-1111-1111-100000000003',
 '러시아 문학 입문하기에도 좋은 책 같습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 8 + TIME '10:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 8 + TIME '10:30:00') AT TIME ZONE 'UTC')),

-- ---- r17 (2개) ----
('44444444-4444-4444-4444-400000000030', '33333333-3333-3333-3333-300000000017', '11111111-1111-1111-1111-100000000004',
 '부조리라는 주제, 처음 만나면 충격적이죠.', FALSE, NULL, ((current_setting('seed.base_date')::date - 22 + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 22 + TIME '12:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000031', '33333333-3333-3333-3333-300000000017', '11111111-1111-1111-1111-100000000007',
 '저도 한번 읽어보겠습니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 22 + TIME '12:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 22 + TIME '12:30:00') AT TIME ZONE 'UTC')),

-- ---- r19 (2개) ----
('44444444-4444-4444-4444-400000000032', '33333333-3333-3333-3333-300000000019', '11111111-1111-1111-1111-100000000001',
 '오래된 리뷰지만 좋네요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 64 + TIME '11:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 64 + TIME '11:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000033', '33333333-3333-3333-3333-300000000019', '11111111-1111-1111-1111-100000000004',
 '가족의 변화 묘사 부분, 정말 서늘합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 63 + TIME '10:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 63 + TIME '10:00:00') AT TIME ZONE 'UTC')),

-- ---- r22 (2개) ----
('44444444-4444-4444-4444-400000000034', '33333333-3333-3333-3333-300000000022', '11111111-1111-1111-1111-100000000004',
 '황무지 묘사가 한 폭의 그림 같아요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 79 + TIME '13:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 79 + TIME '13:00:00') AT TIME ZONE 'UTC')),
('44444444-4444-4444-4444-400000000035', '33333333-3333-3333-3333-300000000022', '11111111-1111-1111-1111-100000000007',
 '의외로 친근한 시점이네요.', FALSE, NULL, ((current_setting('seed.base_date')::date - 79 + TIME '14:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 79 + TIME '14:00:00') AT TIME ZONE 'UTC')),

-- ---- r08 (1개) ----
('44444444-4444-4444-4444-400000000036', '33333333-3333-3333-3333-300000000008', '11111111-1111-1111-1111-100000000003',
 '홀든의 외로움 부분 공감합니다.', FALSE, NULL, ((current_setting('seed.base_date')::date - 3 + TIME '17:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '17:00:00') AT TIME ZONE 'UTC'));

-- ===========================================================================
-- 5) REVIEW_LIKES (36건)
--    (review_id, user_id) UNIQUE 제약 준수
--    논리삭제 리뷰(r20, r21)에는 좋아요 없음
--    논리삭제 유저(u8)는 좋아요 활동 없음
-- ===========================================================================
INSERT INTO review_likes (id, review_id, user_id, created_at, updated_at) VALUES

-- ---- r03 (6) ----
('55555555-5555-5555-5555-500000000001', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date + TIME '14:35:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:35:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000002', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date + TIME '14:50:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:50:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000003', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date + TIME '15:05:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:05:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000004', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date + TIME '15:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:20:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000005', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000006', ((current_setting('seed.base_date')::date + TIME '15:35:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:35:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000006', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000007', ((current_setting('seed.base_date')::date + TIME '16:05:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '16:05:00') AT TIME ZONE 'UTC')),

-- ---- r01 (5) ----
('55555555-5555-5555-5555-500000000007', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date + TIME '09:35:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '09:35:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000008', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000003', ((current_setting('seed.base_date')::date + TIME '09:50:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '09:50:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000009', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date + TIME '10:05:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:05:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000010', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date + TIME '10:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:20:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000011', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000006', ((current_setting('seed.base_date')::date + TIME '10:35:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:35:00') AT TIME ZONE 'UTC')),

-- ---- r02 (4) ----
('55555555-5555-5555-5555-500000000012', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date + TIME '11:05:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:05:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000013', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000003', ((current_setting('seed.base_date')::date + TIME '11:25:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:25:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000014', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date + TIME '11:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:45:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000015', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000007', ((current_setting('seed.base_date')::date + TIME '12:05:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '12:05:00') AT TIME ZONE 'UTC')),

-- ---- r05 (3) ----
('55555555-5555-5555-5555-500000000016', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date - 3 + TIME '10:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '10:30:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000017', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date - 3 + TIME '10:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '10:45:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000018', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date - 3 + TIME '11:15:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '11:15:00') AT TIME ZONE 'UTC')),

-- ---- r11 (3) ----
('55555555-5555-5555-5555-500000000019', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date - 15 + TIME '10:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '10:30:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000020', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date - 15 + TIME '10:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '10:45:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000021', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date - 15 + TIME '11:15:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '11:15:00') AT TIME ZONE 'UTC')),

-- ---- r04 (2) ----
('55555555-5555-5555-5555-500000000022', '33333333-3333-3333-3333-300000000004', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date + TIME '15:10:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:10:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000023', '33333333-3333-3333-3333-300000000004', '11111111-1111-1111-1111-100000000003', ((current_setting('seed.base_date')::date + TIME '15:25:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:25:00') AT TIME ZONE 'UTC')),

-- ---- r06 (2) ----
('55555555-5555-5555-5555-500000000024', '33333333-3333-3333-3333-300000000006', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date - 4 + TIME '14:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 4 + TIME '14:30:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000025', '33333333-3333-3333-3333-300000000006', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date - 4 + TIME '14:50:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 4 + TIME '14:50:00') AT TIME ZONE 'UTC')),

-- ---- r08 (2) ----
('55555555-5555-5555-5555-500000000026', '33333333-3333-3333-3333-300000000008', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date - 3 + TIME '16:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '16:30:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000027', '33333333-3333-3333-3333-300000000008', '11111111-1111-1111-1111-100000000003', ((current_setting('seed.base_date')::date - 3 + TIME '16:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '16:45:00') AT TIME ZONE 'UTC')),

-- ---- r13 (2) ----
('55555555-5555-5555-5555-500000000028', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date - 10 + TIME '14:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '14:30:00') AT TIME ZONE 'UTC')),
('55555555-5555-5555-5555-500000000029', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date - 10 + TIME '14:45:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '14:45:00') AT TIME ZONE 'UTC')),

-- ---- r07 (1) ----
('55555555-5555-5555-5555-500000000030', '33333333-3333-3333-3333-300000000007', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date - 5 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 5 + TIME '11:30:00') AT TIME ZONE 'UTC')),

-- ---- r09 (1) ----
('55555555-5555-5555-5555-500000000031', '33333333-3333-3333-3333-300000000009', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date - 2 + TIME '09:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 2 + TIME '09:30:00') AT TIME ZONE 'UTC')),

-- ---- r10 (1) ----
('55555555-5555-5555-5555-500000000032', '33333333-3333-3333-3333-300000000010', '11111111-1111-1111-1111-100000000005', ((current_setting('seed.base_date')::date - 6 + TIME '18:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 6 + TIME '18:30:00') AT TIME ZONE 'UTC')),

-- ---- r12 (1) ----
('55555555-5555-5555-5555-500000000033', '33333333-3333-3333-3333-300000000012', '11111111-1111-1111-1111-100000000003', ((current_setting('seed.base_date')::date - 12 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 12 + TIME '11:30:00') AT TIME ZONE 'UTC')),

-- ---- r14 (1) ----
('55555555-5555-5555-5555-500000000034', '33333333-3333-3333-3333-300000000014', '11111111-1111-1111-1111-100000000002', ((current_setting('seed.base_date')::date - 8 + TIME '09:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 8 + TIME '09:30:00') AT TIME ZONE 'UTC')),

-- ---- r15 (1) ----
('55555555-5555-5555-5555-500000000035', '33333333-3333-3333-3333-300000000015', '11111111-1111-1111-1111-100000000001', ((current_setting('seed.base_date')::date - 18 + TIME '13:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 18 + TIME '13:30:00') AT TIME ZONE 'UTC')),

-- ---- r17 (1) ----
('55555555-5555-5555-5555-500000000036', '33333333-3333-3333-3333-300000000017', '11111111-1111-1111-1111-100000000004', ((current_setting('seed.base_date')::date - 22 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 22 + TIME '11:30:00') AT TIME ZONE 'UTC'));

-- ===========================================================================
-- 6) NOTIFICATIONS (10건) - 좋아요/댓글 알림 + 인기리뷰 진입 알림 1건
-- ===========================================================================
INSERT INTO notifications (id, review_id, user_id, content,
                           is_read, confirmed_at, created_at, updated_at) VALUES

-- N01: r03(u3 작성) + 김민준(u1) 좋아요 → u3에게 미확인 알림
('66666666-6666-6666-6666-600000000001', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000003',
 '김민준님이 회원님의 리뷰를 좋아합니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '14:35:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:35:00') AT TIME ZONE 'UTC')),

-- N02: r03(u3) + 이서연(u2) 좋아요 → u3에게 미확인
('66666666-6666-6666-6666-600000000002', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000003',
 '이서연님이 회원님의 리뷰를 좋아합니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '14:50:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '14:50:00') AT TIME ZONE 'UTC')),

-- N03: r03(u3) + 최예린(u4) 댓글 → u3에게 미확인
('66666666-6666-6666-6666-600000000003', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000003',
 '최예린님이 회원님의 리뷰에 댓글을 남겼습니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '15:00:00') AT TIME ZONE 'UTC')),

-- N04: r01(u1) + 정도현(u5) 좋아요 → u1에게 (확인 완료)
('66666666-6666-6666-6666-600000000004', '33333333-3333-3333-3333-300000000001', '11111111-1111-1111-1111-100000000001',
 '정도현님이 회원님의 리뷰를 좋아합니다.',
 TRUE, ((current_setting('seed.base_date')::date + TIME '12:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '10:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '12:00:00') AT TIME ZONE 'UTC')),

-- N05: r02(u2) + 박지훈(u3) 좋아요 → u2에게 미확인
('66666666-6666-6666-6666-600000000005', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000002',
 '박지훈님이 회원님의 리뷰를 좋아합니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '11:25:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:25:00') AT TIME ZONE 'UTC')),

-- N06: r02(u2) + 박지훈(u3) 댓글 → u2에게 (확인 완료)
('66666666-6666-6666-6666-600000000006', '33333333-3333-3333-3333-300000000002', '11111111-1111-1111-1111-100000000002',
 '박지훈님이 회원님의 리뷰에 댓글을 남겼습니다.',
 TRUE, ((current_setting('seed.base_date')::date + TIME '13:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '11:20:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '13:00:00') AT TIME ZONE 'UTC')),

-- N07: r05(u4) + 이서연(u2) 댓글 → u4에게 (확인 완료, weekly)
('66666666-6666-6666-6666-600000000007', '33333333-3333-3333-3333-300000000005', '11111111-1111-1111-1111-100000000004',
 '이서연님이 회원님의 리뷰에 댓글을 남겼습니다.',
 TRUE, ((current_setting('seed.base_date')::date - 2 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 3 + TIME '11:30:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 2 + TIME '09:00:00') AT TIME ZONE 'UTC')),

-- N08: r03(u3)이 DAILY 인기 리뷰 1위 진입 → u3에게 미확인 (인기리뷰 알림)
('66666666-6666-6666-6666-600000000008', '33333333-3333-3333-3333-300000000003', '11111111-1111-1111-1111-100000000003',
 '회원님의 리뷰가 일간 인기 리뷰 1위에 올랐습니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date + TIME '17:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date + TIME '17:00:00') AT TIME ZONE 'UTC')),

-- N09: r11(u3) + 정도현(u5) 좋아요 → u3에게 (확인 완료, monthly)
('66666666-6666-6666-6666-600000000009', '33333333-3333-3333-3333-300000000011', '11111111-1111-1111-1111-100000000003',
 '정도현님이 회원님의 리뷰를 좋아합니다.',
 TRUE, ((current_setting('seed.base_date')::date - 14 + TIME '09:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 15 + TIME '11:15:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 14 + TIME '09:00:00') AT TIME ZONE 'UTC')),

-- N10: r13(u2) + 최예린(u4) 댓글 → u2에게 미확인 (monthly)
('66666666-6666-6666-6666-600000000010', '33333333-3333-3333-3333-300000000013', '11111111-1111-1111-1111-100000000002',
 '최예린님이 회원님의 리뷰에 댓글을 남겼습니다.',
 FALSE, NULL, ((current_setting('seed.base_date')::date - 10 + TIME '15:00:00') AT TIME ZONE 'UTC'), ((current_setting('seed.base_date')::date - 10 + TIME '15:00:00') AT TIME ZONE 'UTC'));
