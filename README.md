# sb10-deokhugam-team01

## Codecov

[![codecov](https://codecov.io/github/sb10-part03-team01/sb10-deokhugam-team01/graph/badge.svg)](https://codecov.io/github/sb10-part03-team01/sb10-deokhugam-team01)

### [팀 노션 페이지 링크](https://plume-wavelength-88d.notion.site/_-03_-01-0cfa756433c1834eb1b2812a878f46b5?pvs=74)

## 팀원 구성

| 프로필 |                     이름                     |  역할  | 담당 기능                                                                                     |
|:---:|:-----------------------------------------:|:--------:|:--------------------------------------------------------------|
| <img src="https://github.com/hyunjae3458.png" width="100" style="border-radius:50%;"> | **[김현재](https://github.com/hyunjae3458)** | **도서 관리 도메인,<br>README 작성** | **[주요 기능]**<br>- 도서 관리 API 구현 (CRUD 및 커서 페이지네이션)<br>- Naver Book & OCR Space API 연동<br>**[추가/인프라 기능]**<br>- S3 썸네일 업로드 트랜잭션 동기화 |
| <img src="https://github.com/mjohn26.png" width="100" style="border-radius:50%;"> |   **[문정환](https://github.com/mjohn26)**   | **리뷰 관리, 인기 리뷰 도메인,<br>발표** | **[주요 기능]**<br>- 리뷰 관리 API 구현<br>- 인기 리뷰 대쉬보드 API 구현<br>**[추가/인프라 기능]**<br>- (도메인 비즈니스 로직 고도화 집중) |
| <img src="https://github.com/raonPsm.png" width="100" style="border-radius:50%;"> |   **[박승민](https://github.com/raonPsm)**   | **유저 관리 도메인, 인프라 설정,<br>PPT 제작** | **[주요 기능]**<br>- 유저 관리 API 구현<br>**[추가/인프라 기능]**<br>- AWS 인프라 환경 구축 및 GitHub Actions CI/CD 설정<br>- CodeRabbit(AI 리뷰), Codecov(테스트 커버리지) 연동 |
| <img src="https://github.com/Atory0206.png" width="100" style="border-radius:50%;"> |  **[안승리](https://github.com/Atory0206)**  | **알림 관리, 파워 유저 도메인,<br>노션 정리** | **[주요 기능]**<br>- 알림 관리 API 구현<br>- 파워 유저 대쉬보드 API 구현<br>**[추가/인프라 기능]**<br>- (도메인 비즈니스 로직 고도화 집중) |
| <img src="https://github.com/jongin-git.png" width="100" style="border-radius:50%;"> | **[최종인](https://github.com/jongin-git)**  | **댓글 관리, 인기 도서 도메인,<br>시연 영상 제작** | **[주요 기능]**<br>- 댓글 관리 API 구현<br>- 인기 도서 대쉬보드 API 구현<br>**[추가/인프라 기능]**<br>- MDC 기반 Request ID & IP 로깅 및 헤더 응답 처리 |

---

## 프로젝트 소개

- 덕후감: 도서 이미지 OCR 및 ISBN 매칭 서비스
- 프로젝트 기간: 2026.04.14 ~ 2026.05.08

---

## 배포 사이트

http://3.37.127.27

---

## Swagger UI

[http://3.37.127.27](http://3.37.127.27/swagger-ui/index.html)

---

## 기술 스택

### Backend
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/Spring Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/QueryDSL-0078D4?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/MapStruct-0052CC?style=for-the-badge&logoColor=white">
<img src="https://img.shields.io/badge/Java 17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">

### Database
<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white">
<img src="https://img.shields.io/badge/H2 Database-4169E1?style=for-the-badge">

### Infrastructure
<img src="https://img.shields.io/badge/Amazon AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white">
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">

### CI/CD & Code Quality
<img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
<img src="https://img.shields.io/badge/Codecov-F01F7A?style=for-the-badge&logo=codecov&logoColor=white">
<img src="https://img.shields.io/badge/CodeRabbit-FF6600?style=for-the-badge">

### Collaboration Tools
<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white">
<img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white">

---

## ERD (Entity-Relationship Diagram)

전체 데이터베이스의 구조를 한눈에 파악할 수 있는 전체 ERD입니다.

<img width="1005" height="506" alt="전체 ERD" src="https://github.com/user-attachments/assets/297c2e1e-5976-4aa1-9928-25b59bc0d665" />

---

## 도메인별 ERD 상세

테이블 수가 많아 복잡도를 낮추기 위해 **각 도메인(Domain)별로 구조를 분리**하여 상세히 나타냈습니다.  

<details>
<summary><b>1. 유저(User) 도메인</b></summary>
    
<div markdown="1">
<br>
회원 관리에 필요한 핵심 테이블 구조입니다.
    
```mermaid
erDiagram
    users {
        UUID id PK
        VARCHAR email UK
        VARCHAR nickname
        VARCHAR password
        BOOLEAN is_deleted
        TIMESTAMPTZ deleted_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
</div>
</details>

<details>
<summary><b>2. 도서(Book) 도메인</b></summary>
    
<div markdown="1">
<br>
도서 메타데이터, ISBN 매칭 정보 등 핵심 비즈니스 로직을 담당하는 테이블 구조입니다.
    
```mermaid
erDiagram
    books {
        UUID id PK
        VARCHAR title
        VARCHAR author
        TEXT description
        VARCHAR publisher
        DATE published_date
        VARCHAR isbn UK
        VARCHAR thumbnail_url
        INTEGER review_count
        FLOAT rating
        BOOLEAN is_deleted
        TIMESTAMPTZ deleted_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
</div>
</details>

<details>
<summary><b>3. 리뷰(Review) 도메인</b></summary>
    
<div markdown="1">
<br>
유저가 도서에 남긴 리뷰, 평점, 좋아요 등의 정보를 관리하는 테이블 구조입니다.
    
```mermaid
erDiagram
    books ||--o{ reviews : "has"
    users ||--o{ reviews : "writes"
    users ||--o{ review_likes : "likes"
    reviews ||--o{ review_likes : "receives"

    reviews {
        UUID id PK
        UUID book_id FK
        UUID user_id FK
        VARCHAR content
        FLOAT rating
        INTEGER like_count
        INTEGER comment_count
        BOOLEAN is_deleted
        TIMESTAMPTZ deleted_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    review_likes {
        UUID id PK
        UUID review_id FK
        UUID user_id FK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
</div>
</details>

<details>
<summary><b>4. 댓글(Comment) 도메인</b></summary>
    
<div markdown="1">
<br>
리뷰에 대한 유저 간의 소통(댓글 및 대댓글)을 관리하는 테이블 구조입니다.
    
```mermaid
erDiagram
    reviews ||--o{ comments : "has"
    users ||--o{ comments : "writes"

    comments {
        UUID id PK
        UUID review_id FK
        UUID user_id FK
        VARCHAR content
        BOOLEAN is_deleted
        TIMESTAMPTZ deleted_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
</div>
</details>

<details>
<summary><b>5. 알림(Notification) 도메인</b></summary>
    
<div markdown="1">
<br>
시스템 알림 및 유저 간 상호작용 알림을 처리하는 테이블 구조입니다.
    
```mermaid
erDiagram
    users ||--o{ notifications : "receives"
    reviews ||--o{ notifications : "triggers"

    notifications {
        UUID id PK
        UUID review_id FK
        UUID user_id FK
        VARCHAR content
        BOOLEAN is_read
        TIMESTAMPTZ confirmed_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
</div>
</details>

<details>
<summary><b>6. 대쉬보드 및 통계(Dashboard & Statistics) 도메인</b></summary>
    
<div markdown="1">
<br>
인기 도서, 리뷰 통계, 유저 활동 지표 등 대쉬보드 화면 렌더링과 집계에 최적화된 테이블 구조입니다.
    
```mermaid
erDiagram
    books ||--o{ popular_books : "ranked as"
    reviews ||--o{ popular_reviews : "ranked as"
    users ||--o{ power_users : "ranked as"

    popular_books {
        UUID id PK
        UUID book_id FK
        VARCHAR period_type "DAILY, WEEKLY..."
        DATE calculated_date
        INTEGER rank
        FLOAT score
        FLOAT rating
        INTEGER review_count
        TIMESTAMPTZ created_at
    }
    
    popular_reviews {
        UUID id PK
        UUID review_id FK
        VARCHAR period_type "DAILY, WEEKLY..."
        DATE calculated_date
        INTEGER ranking
        FLOAT score
        INTEGER liked_count
        INTEGER comment_count
        TIMESTAMPTZ created_at
    }
    
    power_users {
        UUID id PK
        UUID user_id FK
        VARCHAR period_type "DAILY, WEEKLY..."
        TIMESTAMPTZ calculated_date
        BIGINT rank
        FLOAT score
        FLOAT review_score_sum
        BIGINT like_count
        BIGINT comment_count
        TIMESTAMPTZ created_at
    }
```
</div>
</details>

---

## 인프라 아키텍처 다이어그램

<img width="970" height="555" alt="스크린샷 2026-05-06 오전 10 42 48" src="https://github.com/user-attachments/assets/23e79095-7c98-41c0-a490-b5719099f181" />

---

## 팀원별 구현 기능 상세

### 김현재

<img width="400" height="350" alt="덕후감_시연영상_2배속 (1)" src="https://github.com/user-attachments/assets/09923e38-defd-4922-962d-abbe9a27f7c5" />


- **도서 관리 API**
    - 도서 등록, 수정, 삭제, 상세 조회 API 구현 및 유효성 검증(Validation) 로직 작성
- **외부 인프라 및 API 연동**
    - AWS S3를 활용한 썸네일 이미지 업로드 및 Presigned URL 발급 로직 구현
    -  네이버 도서 검색 API 연동
    -  OCR Space API 연동

### 문정환

<img width="400" height="350" alt="덕후감_시연영상_2배속 (2)" src="https://github.com/user-attachments/assets/73653057-a8db-4310-b7a8-88466d565311" />


- **리뷰 관리 API**
    - 리뷰 작성, 수정, 삭제 로직 구현
    - 다중 필터링(작성자, 도서, 키워드) 및 커서 기반 페이지네이션을 적용한 리뷰 목록 조회
    - 현재 요청자 기준 리뷰 좋아요 여부(likedByMe) 동적 응답 처리
    - 리뷰 좋아요 토글 기능 및 이벤트 발생 시 타 유저 알림 생성 연동
- **인기 리뷰 대쉬보드 API**
    - 기간별 좋아요 수와 댓글 수를 기반으로 한 인기 리뷰 점수 산정 및 랭킹 부여 로직 구현
    - 기간별 인기 리뷰 목록 조회 및 10위권 진입 시 작성자 알림 발송 로직 연동

### 박승민

<img width="400" height="350" alt="덕후감_시연영상_2배속" src="https://github.com/user-attachments/assets/71f6dffc-0f28-4e79-b1d8-630871bad944" />


- **유저 관리 API**
    - 회원 가입, 로그인 등 유저 관리 도메인 핵심 로직 및 API 구현
- **CI/CD 및 인프라 구축**
    - AWS 기반(ECS, ECR 등) 서버 인프라 환경 구축
    - GitHub Actions를 활용한 CI/CD 자동화 파이프라인 구성
    - Codecov(테스트 커버리지 측정) 및 CodeRabbit(AI 코드 리뷰) 연동을 통한 코드 품질 관리 인프라 셋업

### 안승리

<img width="400" height="350" alt="덕후감_시연영상_2배속 (5)" src="https://github.com/user-attachments/assets/7c8f9843-bca3-4820-a544-6c0093e63149" />

- **알림 관리 API**
    - 알림 생성 및 단건/목록 조회 API 구현
    - 개별 알림 읽음 상태 변경 및 전체 읽음 처리 로직 구현
    - 스케줄링을 활용하여 확인된 지 1주일이 경과한 오래된 알림 자동 삭제 로직 구현
- **파워 유저 및 배치(Batch) 기반 설정**
    - 파워 유저(활동량 우수 유저) 집계 및 대쉬보드 API 구현
    - 통계 데이터 처리를 위한 Spring Batch 공통 설정 및 파일 구조화 작업

### 최종인

<img width="400" height="350" alt="덕후감_시연영상_2배속 (3)" src="https://github.com/user-attachments/assets/431ecbc0-1aba-4203-9e90-654cb1e360ab" />


- **댓글 관리 API**
    - 댓글 CRUD 및 QueryDSL을 활용한 커서 기반 페이지네이션 목록 조회 API 구현
    - 댓글 생성 시 알림 생성 연동
- **인기 도서 대쉬보드 API**
    - 집계 쿼리를 활용한 인기 도서 배치 집계 및 랭킹 저장 로직 구현
    - 기간별 인기 도서 커서 기반 페이지네이션 조회 API 구현
- **시스템 로깅**
    - MDC를 활용한 requestId 및 clientIp 로깅 처리로 요청 추적성 강화

---

## 파일 구조

```Markdown
.
┣ .github
┃ ┣ ISSUE_TEMPLATE
┃ ┣ workflows
┃ ┗ PULL_REQUEST_TEMPLATE.md
┣ src
┃ ┣ main
┃ ┃ ┣ java
┃ ┃ ┃ ┗ com.team01.deokhugam
┃ ┃ ┃   ┣ batch
┃ ┃ ┃   ┣ book
┃ ┃ ┃   ┣ comment
┃ ┃ ┃   ┣ dashboard
┃ ┃ ┃   ┣ global
┃ ┃ ┃   ┃ ┣ config
┃ ┃ ┃   ┃ ┣ constant
┃ ┃ ┃   ┃ ┣ entity
┃ ┃ ┃   ┃ ┣ enums
┃ ┃ ┃   ┃ ┣ exception
┃ ┃ ┃   ┃ ┣ filter
┃ ┃ ┃   ┃ ┣ pagination
┃ ┃ ┃   ┃ ┗ util
┃ ┃ ┃   ┣ notification
┃ ┃ ┃   ┣ review
┃ ┃ ┃   ┗ user
┃ ┃ ┗ resources
┃ ┃     ┣ db
┃ ┃     ┗ static
┃ ┗ test
┃     ┗ java
┃         ┗ com.team01.deokhugam
┃           ┣ batch
┃           ┣ book
┃           ┣ comment
┃           ┣ config
┃           ┣ dashboard
┃           ┣ global
┃           ┣ notification
┃           ┣ review
┃           ┗ user
┣ codecov.yml
┣ coderabbit.yml
┗ README.md
```

---

## 프로젝트 회고록

(제작한 발표자료 링크 혹은 첨부파일 첨부)
