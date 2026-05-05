#!/usr/bin/env bash
# =====================================================================
# 부하 테스트용 ID 풀 추출 스크립트
#
# 테스트 중 DB 에서 매번 SELECT 해서 ID 를 고르면 그 SELECT 자체가 측정값을 왜곡함.
# => 성능 측정 결과의 정확성을 위해 테스트 도중에 DB를 접근하지 않도록 미리 요청용 ID를 추출해서 파일로 저장하는 스크립트
#
# 실행:
#   ./loadtest/scripts/extract-ids.sh
#
# 환경변수 (선택):
#   PGUSER     기본값: deokhugam_user
#   PGDATABASE 기본값: deokhugam_loadtest
#   PGHOST     기본값: localhost
#   SAMPLE_SIZE 기본값: 1000
# 출력:
#   ./data/users.csv
#   ./data/books.csv
#
# 기타:
# loadtest/lib/data.js에서 생성된 CSV 파일을 읽어서 ID 풀로 사용한다.
# =====================================================================

#  -e : 명령어가 0 이 아닌 종료코드를 내면 즉시 스크립트 중단 (psql 이 실패했는데 계속 진행하는 것 방지)
#  -u : 정의되지 않은 변수를 참조하면 에러 (오타 즉시 검출)
#  -o pipefail : 파이프라인 중간 명령이 실패해도 전체 실패로 간주
set -euo pipefail

# 환경변수 기본값 설정
#
#
# SAMPLE_SIZE: 테이블당 몇 개의 ID 를 뽑을지.
#   기본 1000 개. 부하 테스트 시나리오에 따라 조정.
#   - VU(가상 유저) 수 × 충분한 분산 을 고려해서 정함.
#   - 너무 작으면 같은 ID 가 반복 호출되어 캐시 히트율 왜곡.
#   - 너무 크면 추출/CSV 로딩 시간만 늘어남.
PGUSER="${PGUSER:-deokhugam_user}" # DB 에 접속할 사용자 계정 이름
PGDATABASE="${PGDATABASE:-deokhugam_loadtest}" # 접속할 데이터베이스 이름
PGHOST="${PGHOST:-localhost}" # DB 서버가 떠 있는 호스트(서버 주소)
SAMPLE_SIZE="${SAMPLE_SIZE:-1000}"

# 스크립트 위치 기준으로 data 디렉토리 경로 계산
#   ${BASH_SOURCE[0]} : 현재 실행 중인 스크립트 파일의 경로
#   dirname "..."     : 그 경로에서 디렉토리 부분만 추출 (예: /a/b/script.sh -> /a/b)
#   cd "..." && pwd   : 그 디렉토리로 이동 후 절대경로 출력 (상대경로를 절대경로로 정규화)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 스크립트 파일이 있는 디렉토리에서 위로 올라간 다음, 거기 있는 data 폴더를 가리키는 경로를 DATA_DIR 변수에 저장
DATA_DIR="$SCRIPT_DIR/../data"

# 출력 디렉토리가 없으면 생성 (-p 는 이미 있어도 에러 안 냄)
mkdir -p "$DATA_DIR"

# 사용자에게 진행 상황 표시 (실행 환경 확인용)
echo "==> ID 풀 추출 시작"
echo "    DB: $PGDATABASE @ $PGHOST (user=$PGUSER)"
echo "    샘플 크기: $SAMPLE_SIZE 개씩"
echo ""

# extract: 한 테이블의 id 를 뽑아 CSV 로 저장하는 함수
extract() {
  # $1 - 테이블 이름 (users, books, reviews 등
  # local 키워드: 함수 내부 변수임을 명시. 함수 밖의 동명 변수와 충돌 방지.
  local table="$1" # 함수의 첫 번째 인자($1)를 table 변수에 저장
  local out="$DATA_DIR/${table}.csv" # 출력 파일 경로를 조합: DATA_DIR 환경변수 + 테이블명 + .csv 확장자
  echo "    [$table] -> $out"
  # psql 옵션:
  #   -U "$PGUSER"       : 접속 사용자
  #   -h "$PGHOST"       : 호스트
  #   -d "$PGDATABASE"   : DB 이름
  #   -v ON_ERROR_STOP=1 : SQL 에러 발생 시 즉시 중단하고 종료. 기본 동작은 에러 나도 다음 명령 계속 실행.
  #   -c "..."           : 한 줄 SQL 명령 실행 후 종료
  psql -U "$PGUSER" -h "$PGHOST" -d "$PGDATABASE" -v ON_ERROR_STOP=1 \
    -c "\copy (SELECT id FROM $table ORDER BY random() LIMIT $SAMPLE_SIZE) TO '$out' CSV HEADER"
    # \copy vs COPY:
    #   - COPY (서버 측)    : DB 서버의 파일시스템에 저장. superuser 권한 필요.
    #   - \copy (클라이언트) : psql 이 결과를 받아 로컬 파일에 저장. 일반 사용자도 가능.
    #   여기선 \copy 사용 -> 개발자 PC 의 ./data/ 에 직접 저장.

    # SQL 동작:
    #   SELECT id FROM users
    #   ORDER BY random()     -- 무작위 정렬
    #   LIMIT 1000            -- 앞에서 1000 개 자르기
    # => 결과: 무작위로 섞인 1000 개의 id
}

# 대상 테이블별로 추출 실행
extract users
extract books

# wc -l : 각 파일의 라인 수 출력. 실제 추출된 ID 개수 = (라인 수 - 1) <- HEADER 한 줄 제외
#         원했던 SAMPLE_SIZE 만큼 잘 뽑혔는지 빠르게 확인하는 용도.
echo ""
echo "==> 완료"
wc -l "$DATA_DIR"/*.csv
