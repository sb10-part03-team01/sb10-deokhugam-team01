// =====================================================================
// 사전 추출된 ID CSV 로더
//
// SharedArray 사용 이유:
//   - VU 100개가 각자 1,000개 ID 를 메모리에 들고 있으면 100K 중복 데이터
//   - SharedArray 는 모든 VU 가 동일한 메모리 영역 공유 → O(1)
//   - SharedArray: 모든 가상 사용자(VU)가 동일한 메모리를 공유하도록 하는 읽기 전용 배열
//
// CSV 형식: 헤더 1줄 + UUID 1줄씩
//   id
//   3f4b1234-...
//   8a2c5678-...
// =====================================================================

import {SharedArray} from 'k6/data';

// CSV 파일을 읽어 UUID 배열로 반환
function loadCsv(filename) {
  // open() 은 init context 에서만 호출 가능 (default 함수 안에서 X)
  const raw = open(`../data/${filename}`);
  const lines = raw.split('\n');
  return lines
  .slice(1) // 첫 줄 헤더(id) 제거
  .map((l) => l.trim()) // CRLF/공백 제거
  .filter((l) => l.length > 0); // 빈 줄 제거
}

// 일반 배열 사용 시:
//   VU 100개 × ID 1,000개 = 100,000개 중복 데이터 메모리 점유
// SharedArray 사용 시:
//   ID 1,000개를 단 한 번만 메모리에 적재 -> 모든 VU가 참조
export const userIds = new SharedArray('userIds', () => loadCsv('users.csv'));
export const bookIds = new SharedArray('bookIds', () => loadCsv('books.csv'));

// 배열에서 랜덤 요소 1개 반환
// VU 가 매 iteration 마다 랜덤하게 하나 뽑아 쓸 때 사용
export function pickRandom(arr) {
  // 배열이 비어 있으면 undefined를 반환하므로 fail-fast
  if (!arr || arr.length === 0) {
    throw new Error(
        'ID pool is empty. Please run extract-ids.sh and verify CSV files.'
    );
  }
  return arr[Math.floor(Math.random() * arr.length)];
}
