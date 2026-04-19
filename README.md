# sb10-deokhugam-team01

## Codecov

[![codecov](https://codecov.io/github/sb10-part03-team01/sb10-deokhugam-team01/graph/badge.svg?token=VKUCVC9GI7)](https://codecov.io/github/sb10-part03-team01/sb10-deokhugam-team01)

### [팀 노션 페이지 링크](https://plume-wavelength-88d.notion.site/_-03_-01-0cfa756433c1834eb1b2812a878f46b5?pvs=74)

## 팀원 구성

| 프로필 |                    이름                     |    역할    | 담당 기능                                                         |
|:---:|:-----------------------------------------:|:--------:|:--------------------------------------------------------------|
|     | **[김현재](https://github.com/hyunjae3458)** | **null** | **[주요 기능]**<br>- **기능**<br>**[추가/인프라 기능]**<br>- 추가1 <br>- 추가2 |
|     |   **[문정환](https://github.com/mjohn26)**   | **null** | **[주요 기능]**<br>- **기능**<br>**[추가/인프라 기능]**<br>- 추가1 <br>- 추가2 |
|     |   **[박승민](https://github.com/raonPsm)**   | **null** | **[주요 기능]**<br>- **기능**<br>**[추가/인프라 기능]**<br>- 추가1 <br>- 추가2 |
|     |  **[안승리](https://github.com/Atory0206)**  | **null** | **[주요 기능]**<br>- **기능**<br>**[추가/인프라 기능]**<br>- 추가1 <br>- 추가2 |
|     | **[최종인](https://github.com/jongin-git)**  | **null** | **[주요 기능]**<br>- **기능**<br>**[추가/인프라 기능]**<br>- 추가1 <br>- 추가2 |

---

## 프로젝트 소개

- 덕후감: 도서 이미지 OCR 및 ISBN 매칭 서비스
- 프로젝트 기간: 2026.04.14 ~ 2026.05.08

---

## 기술 스택

- Backend: Spring Boot, Spring Data JPA, Java17, Gradle

<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">

- Database: PostgreSQL

<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white">

- 공통 Tool: Git, Github, Discord, IntelliJ IDEA

<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white">
<img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white">

---

## 팀원별 구현 기능 상세

### 김현재

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **소셜 로그인 API**
    - Google OAuth 2.0을 활용한 소셜 로그인 기능 구현
    - 로그인 후 추가 정보 입력을 위한 RESTful API 엔드포인트 개발
- **회원 추가 정보 입력 API**
    - 회원 유형(관리자, 학생)에 따른 조건부 입력 처리 API 구현

### 문정환

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **회원별 권한 관리**
    - Spring Security를 활용하여 사용자 역할에 따른 권한 설정
    - 관리자 페이지와 일반 사용자 페이지를 위한 조건부 라우팅 처리
- **반응형 레이아웃 API**
    - 클라이언트에서 요청된 반응형 레이아웃을 위한 RESTful API 엔드포인트 구현

### 박승민

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **수강생 정보 관리 API**
    - `GET`요청을 사용하여 학생의 수강 정보를 조회하는 API 엔드포인트 개발
    - 학생 정보의 CRUD 처리 (Spring Data JPA 사용)
- **공용 Button API**
    - 공통으로 사용할 버튼 기능을 처리하는 API 엔드포인트 구현

### 안승리

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **관리자 API**
    - `@PathVariable`을 사용한 동적 라우팅 기능 구현
    - `PATCH`,`DELETE`요청을 사용하여 학생 정보를 수정하고 탈퇴하는 API 엔드포인트 개발
- **CRUD 기능**
    - 학생 정보의 CRUD 기능을 제공하는 API 구현 (Spring Data JPA)
- **회원관리 슬라이더**
    - 학생별 정보 목록을`Carousel`형식으로 조회하는 API 구현

### 최종인

(자신이 개발한 기능에 대한 사진이나 gif 파일 첨부)

- **학생 시간 정보 관리 API**
    - 학생별 시간 정보를`GET`요청을 사용하여 조회하는 API 구현
    - 실시간 접속 현황을 관리하는 API 엔드포인트
- **수정 및 탈퇴 API**
    - `PATCH`,`DELETE`요청을 사용하여 수강생의 개인정보 수정 및 탈퇴 처리
- **공용 Modal API**
    - 공통 Modal 컴포넌트를 처리하는 API 구현

---

## 파일 구조

```Markdown
src
┣ main
┃ ┣ java
┃ ┃ ┣ com
┃ ┃ ┃ ┣ example
┃ ┃ ┃ ┃ ┣ controller
┃ ┃ ┃ ┃ ┃ ┣ AuthController.java
┃ ┃ ┃ ┃ ┃ ┣ UserController.java
┃ ┃ ┃ ┃ ┃ ┗ AdminController.java
┃ ┃ ┃ ┃ ┣ model
┃ ┃ ┃ ┃ ┃ ┣ User.java
┃ ┃ ┃ ┃ ┃ ┗ Course.java
┃ ┃ ┃ ┃ ┣ repository
┃ ┃ ┃ ┃ ┃ ┣ UserRepository.java
┃ ┃ ┃ ┃ ┃ ┗ CourseRepository.java
┃ ┃ ┃ ┃ ┣ service
┃ ┃ ┃ ┃ ┃ ┣ AuthService.java
┃ ┃ ┃ ┃ ┃ ┣ UserService.java
┃ ┃ ┃ ┃ ┃ ┗ AdminService.java
┃ ┃ ┃ ┃ ┣ security
┃ ┃ ┃ ┃ ┃ ┣ SecurityConfig.java
┃ ┃ ┃ ┃ ┃ ┗ JwtAuthenticationEntryPoint.java
┃ ┃ ┃ ┃ ┣ dto
┃ ┃ ┃ ┃ ┃ ┣ LoginRequest.java
┃ ┃ ┃ ┃ ┃ ┗ UserResponse.java
┃ ┃ ┃ ┃ ┣ exception
┃ ┃ ┃ ┃ ┃ ┣ GlobalExceptionHandler.java
┃ ┃ ┃ ┃ ┃ ┗ ResourceNotFoundException.java
┃ ┃ ┃ ┃ ┣ utils
┃ ┃ ┃ ┃ ┃ ┣ JwtUtils.java
┃ ┃ ┃ ┃ ┃ ┗ UserMapper.java
┃ ┃ ┃ ┣ resources
┃ ┃ ┃ ┃ ┣ application.properties
┃ ┃ ┃ ┃ ┗ static
┃ ┃ ┃ ┃ ┃ ┣ css
┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
┃ ┃ ┃ ┃ ┃ ┣ js
┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
┃ ┃ ┃ ┣ webapp
┃ ┃ ┃ ┃ ┣ WEB-INF
┃ ┃ ┃ ┃ ┃ ┗ web.xml
┃ ┃ ┃ ┣ test
┃ ┃ ┃ ┃ ┣ java
┃ ┃ ┃ ┃ ┃ ┣ com
┃ ┃ ┃ ┃ ┃ ┃ ┣ example
┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthServiceTest.java
┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserControllerTest.java
┃ ┃ ┃ ┃ ┃ ┃ ┗ ApplicationTests.java
┃ ┃ ┃ ┣ resources
┃ ┃ ┃ ┃ ┣ application.properties
┃ ┃ ┃ ┃ ┗ static
┃ ┃ ┃ ┃ ┃ ┣ css
┃ ┃ ┃ ┃ ┃ ┃ ┗ style.css
┃ ┃ ┃ ┃ ┃ ┣ js
┃ ┃ ┃ ┃ ┃ ┃ ┗ script.js
┣ pom.xml
┣ Application.java
┣ application.properties
┣ .gitignore
┗ README.md
```

---

## 구현 홈페이지

(개발한 홈페이지에 대한 링크 게시)
https://www.codeit.kr/

---

## 프로젝트 회고록

(제작한 발표자료 링크 혹은 첨부파일 첨부)
