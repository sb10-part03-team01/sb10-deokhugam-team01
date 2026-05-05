// =====================================================================
// k6 부하 테스트 공통 설정
//
// 환경변수:
//   BASE_URL : 대상 서버 URL (기본 http://localhost:8080)
//   MODE     : load (현재 1종만 정의됨, 향후 stress/soak 추가 가능)
//
// =====================================================================

// __ENV : k6 가 외부 환경변수를 노출하는 객체 - 환경변수 미지정 시 안전한 로컬 기본값 사용
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const MODE = __ENV.MODE || 'load';

// ---------------------------------------------------------------------
// MODE 별 stages (시간대별 VU 변화 프로파일)
//
// stages 의미:
//   { duration: '1m', target: 30 }
//   = 1분 동안 현재 VU 수 → 30 으로 점진적 증가/감소
//
// 마지막 stage 는 target=0 으로 끝내는 게 권장:
//   - 측정 종료 시 점진 하강하며 자원 정리
//   - 갑작스런 종료로 인한 graceful shutdown 미처리 방지
// ---------------------------------------------------------------------
const STAGES = {
  // load: 베이스라인 측정용 (정상 트래픽 가정)
  //   1m 상승 -> 3m 유지 -> 1m 하강 = 총 5분
  //   '평상시 응답 시간이 어느 정도인가' 를 보는 용도
  //   30 VU 는 임의값 — 운영 트래픽 데이터 확보 시 그에 맞춰 조정
  load: [
    {duration: '1m', target: 30},
    {duration: '3m', target: 30},
    {duration: '1m', target: 0},
  ],
};

// MODE 에 해당하는 stages 배열 반환
// 잘못된 MODE 값이면 즉시 throw -> 시나리오 시작 전에 실패 (fail-fast)
export function getStages() {
  const stages = STAGES[MODE];
  if (!stages) {
    throw new Error(
        `Unknown MODE: ${MODE}. Use one of: ${Object.keys(STAGES).join(', ')}`);
  }
  return stages;
}

// ---------------------------------------------------------------------
// SLO (Service Level Objective) 임계값
//
// 임계값 미달 시 k6 종료 코드가 0가 아닐 수 있음 -> CI 에서 빌드 실패 처리 가능.
// 즉 '성능 저하 방지 장치' 로 기능함.
//
// 표기법:
//   'p(95)<300' : 95 백분위수 응답시간이 300ms 미만이어야 함
//                 (전체 요청 중 95% 가 300ms 안에 끝나야 한다는 뜻)
//   'rate<0.01' : 에러율이 1% 미만이어야 함
//
// 참고: SLO 설정은 서비스 특성에 맞게 조정 필요
// ---------------------------------------------------------------------

// 읽기 API: 일반 웹 API 권장값
// (https://medium.com/@jfindikli/the-ultimate-guide-to-faster-api-response-times-p50-p90-p99-latencies-0fb60f0a0198)
export const THRESHOLDS_READ = {
  http_req_failed: ['rate<0.01'], // 실패율 1% 미만
  // 백분위95 300ms, 백분위99 800ms 이내
  http_req_duration: ['p(95)<300', 'p(99)<800'],
  // p(95) < 300ms -> 대부분 사용자 경험 보장
  // p(99) < 800ms -> 극소수 느린 요청도 허용 범위 내로 관리
};
