# CTE + STRAIGHT_JOIN 회고 시리즈 인덱스

## 시리즈 목적

이력서에 적은 실무 경험을 "주장"이 아니라 "검증 가능한 근거"로 설명하기 위해 작성한 3편 시리즈다.

## 읽는 순서

1. [사건편: EXPLAIN 이상 징후와 당시 대응](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-01-incident-straight-join.md)
2. [원인편: 구조적으로 왜 join 순서가 흔들렸는가](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-02-root-cause-structure.md)
3. [현재편: 지금이라면 어떤 순서로 해결할 것인가](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-03-what-i-would-do-now.md)

## 핵심 근거

- [case-003 재현 코드](https://github.com/elyo9381/TIL/tree/master/problem-solving-cases/case-003-join-hint-repro)
- [사건편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-01-incident-straight-join.md)
- [원인편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-02-root-cause-structure.md)
- [현재편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-03-what-i-would-do-now.md)

## 검증 명령

```bash
cd problem-solving-cases
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
curl "http://localhost:8080/api/case-003/join-hint/report?serviceType=VIP&limit=20"
curl "http://localhost:8080/api/case-003/join-hint/benchmark?serviceType=VIP&limit=20&iterations=10"
```

## 게시 전 체크

- 사건편에서 "당시 액션"과 "한계"를 분리했는가
- 원인편에서 개인 실수를 과장하지 않고 구조 압력으로 설명했는가
- 현재편에서 힌트를 마지막 수단으로 배치했는가
- 모든 주장에 코드/테스트/문서 링크가 있는가

## 범위와 한계

- 공개 저장소에는 재현 코드와 3편 본문만 유지한다.
- 내부 분석/드래프트 문서는 비공개로 분리했다.
- 당시 운영 DB 절대 성능 지표(P95/P99, QPS, CPU)는 확정하지 않는다.
