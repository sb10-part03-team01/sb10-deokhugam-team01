## QA 시드 데이터

명세의 모든 필수 기능(목록 조회, 검색, 정렬, 페이지네이션, 인기/파워 랭킹, 알림)을
한 번에 검증할 수 있는 개발용 시드 데이터를 제공한다.

### 적재 방식

- 시드 SQL: `src/main/resources/db/seed-qa.sql`
- qa 프로파일에서만 자동 적재된다 (`spring.sql.init.data-locations`)
- dev / test / prod 프로파일에서는 적재되지 않는다 (prod는 `spring.sql.init.mode: never` 안전 가드)

| 프로파일   | 시드 적재 | 용도                    |
|--------|-------|-----------------------|
| `qa`   | ✅     | 통합 QA / 시연 / 정렬·랭킹 검증 |
| `dev`  | ❌     | 개발자 자유 입력             |
| `test` | ❌     | 단위·통합 테스트 (H2)        |
| `prod` | ❌     | 운영 (안전 가드)            |

### 사전 준비

#### 1. 로컬 PostgreSQL

- 로컬에 PostgreSQL이 설치되어 있고 `5432` 포트로 접근 가능할 것
- `deokhugam_db` 데이터베이스가 생성되어 있을 것

```bash
# 예시 — 데이터베이스 생성
createdb deokhugam_db
```

#### 2. `.env` 파일

프로젝트 루트의 `.env`에 connection 정보를 작성한다 (이미 있다면 그대로).

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/deokhugam_db
SPRING_DATASOURCE_USERNAME=<your_pg_user>
SPRING_DATASOURCE_PASSWORD=<your_pg_password>
SERVER_PORT=8080
```

> `application.yaml`의 `spring.config.import: optional:file:.env[.properties]` 설정으로
> `.env`의 키-값이 환경변수처럼 자동 주입된다.

### 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=qa'
```

부팅 흐름:

1. `schema.sql` 자동 실행 → 테이블이 없으면 생성 (`IF NOT EXISTS`)
2. `db/seed-qa.sql` 실행
    - 맨 위 `TRUNCATE`로 기존 데이터 정리
    - 시드 데이터 INSERT
3. Spring Boot 부팅 완료 (`Started DeokhugamApplication in N seconds`)

### 데이터 분량

| 엔티티          | 개수 | 비고                                               |
|--------------|----|--------------------------------------------------|
| User         | 8  | 활성 7명 + 논리삭제 1명. 비밀번호는 모두 `deokhugam1!` (BCrypt) |
| Book         | 13 | 12권의 고전 명작 + ISBN UNIQUE 검증용 더미 1권               |
| Review       | 24 | 논리삭제 2건 포함                                       |
| Comment      | 36 | 리뷰별 0~6개 분포                                      |
| ReviewLike   | 36 | (review_id, user_id) UNIQUE 준수                   |
| Notification | 10 | 좋아요 / 댓글 / 인기 리뷰 진입 알림                           |

### 시간 분산 — 부팅한 날이 항상 D-0

시드 안의 모든 timestamp는 부팅 시점의 `CURRENT_DATE`를 기준으로 한 상대 표현식으로 작성되어 있어,
어떤 날 부팅해도 DAILY/WEEKLY/MONTHLY/ALL_TIME 분류가 동일하게 재현된다.

- DAILY (D-0): 리뷰 4건
- WEEKLY (D-2 ~ D-6): 리뷰 6건
- MONTHLY (D-8 ~ D-25): 리뷰 8건
- ALL_TIME (D-65 ~ D-100): 리뷰 6건 (논리삭제 2건 포함)
- USER 가입일: 프로토타입 D-177, 일반 D-147, 논리삭제 계정 D-26 가입 / D-2 탈퇴

내부 구현은 시드 맨 위의 `SELECT set_config('seed.base_date', CURRENT_DATE::text, false);`로
세션 변수에 base_date를 한 번 핀(pin)한 뒤 모든 INSERT가 이를 참조하는 방식.

### 데이터 초기화 / 재적재

#### 재적재

`qa` 프로파일로 다시 부팅하기만 하면 된다. 시드 맨 위의
`TRUNCATE TABLE ... RESTART IDENTITY CASCADE`가 매 부팅마다 실행되므로
**항상 깨끗한 상태에서 시드가 다시 적재**된다.

#### 완전 초기화

데이터베이스 자체를 비우고 싶다면:

```bash
dropdb deokhugam_db && createdb deokhugam_db./gradlew bootRun --args='--spring.profiles.active=qa'
```

### 프로토타입 로그인 계정

| 닉네임 | 이메일                        | 비밀번호          |
|-----|----------------------------|---------------|
| 김민준 | minjun.kim@deokhugam.test  | `deokhugam1!` |
| 이서연 | seoyeon.lee@deokhugam.test | `deokhugam1!` |
| 박지훈 | jihoon.park@deokhugam.test | `deokhugam1!` |
| 최예린 | yerin.choi@deokhugam.test  | `deokhugam1!` |
| 정도현 | dohyun.jung@deokhugam.test | `deokhugam1!` |
