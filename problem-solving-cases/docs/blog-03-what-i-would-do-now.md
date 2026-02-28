# [현재편] 지금이라면 STRAIGHT_JOIN 전에 무엇을 먼저 할까

## 한 줄 요약

지금 같은 문제가 다시 오면, 힌트를 바로 쓰지 않고 `계측 -> 인덱스/정렬 경로 정비 -> 쿼리 분리 -> 필요 시 모델 개선` 순서로 해결한다.

## 1. 증상 계측을 먼저 고정

먼저 "무엇이 느린지"를 고정한다.

- 조건: 같은 serviceType, 같은 limit, 같은 데이터 볼륨
- 지표: p95/p99, rows examined, 실행계획 마커
- 기준: 변경 전/후를 같은 시나리오로 비교

재현 API:

- `GET /api/case-003/join-hint/report?serviceType=VIP&limit=20`
- `GET /api/case-003/join-hint/benchmark?serviceType=VIP&limit=20&iterations=10`

## 2. 인덱스/정렬 경로 먼저 정비

대부분의 경우 힌트보다 먼저 봐야 하는 건 인덱스와 정렬 경로다.

- where + order by 조합과 인덱스 정합성
- 필터가 어느 조인 단계에서 적용되는지
- 불필요한 임시 정렬이 생기는지

여기서 해결되면 힌트 없이도 안정적이다.

## 3. 쿼리 분리

하나의 큰 쿼리에 모든 책임을 몰지 않는다.

1. 페이지 대상 ID를 먼저 고정
2. 상세 조인/집계를 후속 조회로 분리

이 방식은 옵티마이저 부담을 줄이고, 실행계획 예측 가능성을 높인다.

## 4. 필요 시 읽기 모델/사전 집계

요구가 계속 커진다면, 조회 모델을 분리한다.

- read model
- 캐시 가능한 집계 테이블
- 배치 선계산

핵심은 "OLTP 쿼리 하나에 모든 요청을 실지 않기"다.

## 5. 그래도 필요하면 힌트

`STRAIGHT_JOIN`은 마지막 수단으로 제한 적용한다.

- 적용 범위 최소화
- 모니터링 기준 명시
- 되돌림 조건을 같이 준비

즉, 힌트를 쓰더라도 "통제 가능한 실험"으로 사용해야 한다.

## 검증 가능해야 주장 가능하다

내가 이번에 고정한 기준은 아래다.

```bash
cd problem-solving-cases
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
./gradlew clean test --rerun-tasks
```

근거 문서:

- [사건편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-01-incident-straight-join.md)
- [원인편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-02-root-cause-structure.md)

## 결론

회고의 목적은 과거 선택을 미화하는 게 아니다.  
같은 문제를 다시 만났을 때 더 좋은 순서와 기준으로 해결하는 것이다.
