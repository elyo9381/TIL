# CTE + STRAIGHT_JOIN 회고 시리즈 인덱스

## 시리즈 목적

이력서에 적은 실무 경험을 "주장"이 아니라 "검증 가능한 근거"로 설명하기 위해 작성한 3편 시리즈다.

## 읽는 순서

1. [사건편: EXPLAIN 이상 징후와 당시 대응](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-01-incident-straight-join.md)
2. [원인편: 구조적으로 왜 join 순서가 흔들렸는가](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-02-root-cause-structure.md)
3. [현재편: 지금이라면 어떤 순서로 해결할 것인가](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-03-what-i-would-do-now.md)

## 핵심 근거

- [글 구조 설계](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-structure-cte-straight-join-repro.md)
- [메인 드래프트](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-draft-cte-straight-join-repro.md)
- [최종 회고본](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-final-movv-join-hint-retrospective.md)
- [타임라인 분석](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/movv-maas-global-timeline-analysis.md)
- [포렌식 리포트](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/movv-cte-straight-join-forensic-report.md)

## 검증 명령

```bash
cd /Users/yowon/study/til/TIL/problem-solving-cases
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
curl "http://localhost:8080/api/case-003/join-hint/report?serviceType=VIP&limit=20"
curl "http://localhost:8080/api/case-003/join-hint/benchmark?serviceType=VIP&limit=20&iterations=10"
```

## 게시 전 체크

- 사건편에서 "당시 액션"과 "한계"를 분리했는가
- 원인편에서 개인 실수를 과장하지 않고 구조 압력으로 설명했는가
- 현재편에서 힌트를 마지막 수단으로 배치했는가
- 모든 주장에 코드/테스트/문서 링크가 있는가

## Changelog

- 변경된 내용:
  - `case-003`에 benchmark API와 `EXPLAIN JSON` 근거를 연결했다.
  - 사건/원인/현재 3편으로 분리해 한 글의 주장 범위를 좁혔다.
  - 면접 검증용 `Evidence Packet`을 추가했다.
- 근거 기반으로 확정 가능한 것:
  - CTE 전역 사용 패턴
  - Reservation 축의 `STRAIGHT_JOIN` 도입 이력
  - 재현 코드에서 hint off/on 플랜 차이
- 남는 가정/한계:
  - 당시 운영 DB 절대 성능 지표(P95/P99, QPS, CPU)는 확정하지 않는다.
