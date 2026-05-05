// =====================================================================
// 인기 리뷰 조회 (GET /api/reviews/popular) 전용 부하 시나리오
//
// 목적: 단일 엔드포인트 베이스라인 측정 -> 개선 -> 재측정 비교
//       (개선 후보: 집계 캐싱, 인덱스 튜닝, 사전 계산 테이블 등)
//
// period 는 일단 WEEKLY 만 테스트
//
// 실행:
//   MODE=load k6 run loadtest/scenarios/popular-reviews.js
// =====================================================================

import {check, sleep} from 'k6';
import {getStages, THRESHOLDS_READ} from '../lib/config.js';
import {get} from '../lib/http.js';
import {pickRandom, userIds} from '../lib/data.js';

export const options = {
  stages: getStages(),
  thresholds: THRESHOLDS_READ,
};

export default function () {

  const userId = pickRandom(userIds);

  // 인기 리뷰 주간 랭킹 50건 조회
  const r = get('/api/reviews/popular?period=WEEKLY&limit=50', userId);
  check(r, {'200': (res) => res.status === 200});

  // 가상 유저(VU)가 한 요청을 보낸 뒤 1초 동안 기다린 후 다음 요청을 보내게 함 (현실 반영)
  sleep(1);
}
