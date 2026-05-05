// =====================================================================
// HTTP 헬퍼 — 인증 헤더 자동 부착 + URL 마스킹
//
// 왜 필요한가:
//   1) 모든 요청에 'Deokhugam-Request-User-ID' 헤더 필수
//   2) k6 메트릭은 URL 별로 집계되므로 /books/{uuid1}, /books/{uuid2} 가
//      서로 다른 엔드포인트로 잡힘 -> 마스킹해서 /books/:id 로 통합해야 그래프가 의미를 가짐
// =====================================================================

import http from 'k6/http';
import {BASE_URL} from './config.js';

// UUID 패턴 정규식
// gi: 플래그
//   - g: 전체 문자열에서 모든 매치 찾기
//   - i: 대소문자 구분 없이 매치
const UUID_RE = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/gi;

// URL에서 UUID를 :id로 치환하여 k6 메트릭 집계 단위를 통일
//   /api/books/3f4b...e2 -> /api/books/:id
//   BASE_URL 제거, 쿼리스트링 제거, UUID -> :id 순서로 처리
function maskUrl(url) {
  return url.replace(BASE_URL, '').split('?')[0].replace(UUID_RE, ':id');
}

// 공통 요청 파라미터 생성
//   - 모든 요청에 인증 헤더(Deokhugam-Request-User-ID) 자동 부착
//   - tags.name으로 마스킹된 URL을 지정 -> Grafana에서 엔드포인트별 집계 가능
function buildParams(userId, name) {
  return {
    headers: {
      'Deokhugam-Request-User-ID': userId,
      'Content-Type': 'application/json',
    },
    // tags.name 으로 그룹핑 — 메트릭에서 같은 엔드포인트로 집계됨
    tags: {name: name},
  };
}

// GET 요청
export function get(path, userId) {
  const url = `${BASE_URL}${path}`;
  return http.get(url, buildParams(userId, `GET ${maskUrl(url)}`));
}

// POST 요청
export function post(path, body, userId) {
  const url = `${BASE_URL}${path}`;
  return http.post(url, JSON.stringify(body),
      buildParams(userId, `POST ${maskUrl(url)}`));
}

// PATCH 요청
export function patch(path, body, userId) {
  const url = `${BASE_URL}${path}`;
  return http.patch(url, JSON.stringify(body),
      buildParams(userId, `PATCH ${maskUrl(url)}`));
}

// DELETE 요청
export function del(path, userId) {
  const url = `${BASE_URL}${path}`;
  return http.del(url, null, buildParams(userId, `DELETE ${maskUrl(url)}`));
}
