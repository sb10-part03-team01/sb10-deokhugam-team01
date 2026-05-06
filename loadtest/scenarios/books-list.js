// =====================================================================
// 도서 목록 조회 (GET /api/books) 전용 부하 시나리오
//
// 목적: 단일 엔드포인트 베이스라인 측정 -> 개선 -> 재측정 비교
//      (개선 후보: 인덱스 추가, N+1 제거, 캐시 도입 등)
//
// 실행:
//   MODE=load k6 run loadtest/scenarios/books-list.js
//
// MODE 별 부하 프로파일은 lib/config.js 의 getStages() 참고.
// =====================================================================

import {check, sleep} from 'k6';
import {getStages, THRESHOLDS_READ} from '../lib/config.js';
import {get} from '../lib/http.js';
import {pickRandom, userIds} from '../lib/data.js';

// k6 옵션: 부하 프로파일과 합격 기준
// - stages       : MODE 환경변수에 따라 동적으로 결정 (smoke/load/stress)
// - thresholds   : 읽기 API 공통 SLO (p95 응답시간, 에러율 등)
export const options = {
  stages: getStages(),
  thresholds: THRESHOLDS_READ,
};

export default function () {
  // 추출된 ID 풀에서 무작위 사용자 선택
  // - 같은 userId 만 반복 호출하면 실제 운영 부하를 흉내내지 못함
  const userId = pickRandom(userIds);

  // 도서 목록 50건 조회
  const r = get('/api/books?limit=50', userId);
  check(r, {'200': (res) => res.status === 200});

  sleep(1);
}
