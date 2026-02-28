# [사건편] EXPLAIN이 틀어졌을 때 STRAIGHT_JOIN으로 급한 불을 끈 이유

> 내부 코드/운영 데이터는 비공개 원칙을 지키고, 비식별화된 구조와 재현 코드만 공개합니다.

## 한 줄 요약

당시 내가 한 핵심 액션은 CTE 자체를 바꾼 게 아니라, `EXPLAIN`에서 확인한 조인 진입점 불일치를 `STRAIGHT_JOIN`(+일부 `FORCE INDEX`)으로 단기 안정화한 것이다.

## 당시 상황

예약 조회 응답이 불안정했고, 배포 리스크를 짧은 시간에 줄여야 했다.  
이 시점의 우선순위는 "완성도 높은 재설계"보다 "운영 안정화"였다.

문제는 쿼리 문법이 아니라 실행계획 변동성이었다.

- 어떤 조건에서는 기대하던 테이블(`tb_reserv`)부터 진입하지 않았고
- 임시 테이블/파일 정렬 비용이 커지는 신호가 반복됐다

## 내가 실제로 한 일

1. 핵심 예약 조회 쿼리에 `STRAIGHT_JOIN` 적용
2. 정렬 경로에 일부 `FORCE INDEX` 적용
3. 필터 조건(serviceType 계열)과 조인 구성 정리

핵심은 "영구 정답"이 아니라 "실패 확률을 빨리 낮추는 단기 통제"였다.

## 왜 이 선택이 당시엔 합리적이었나

- 원인 후보가 `EXPLAIN`으로 빠르게 확인 가능했다
- SQL 레이어에서 국소 변경으로 대응할 수 있었다
- 대규모 모델 변경 없이 릴리즈 가능했다

실무에서는 이상적인 해법보다, 제약 안에서 위험을 빠르게 줄이는 선택이 필요한 순간이 있다.

## 하지만 여기서 끝나면 안 되는 이유

`STRAIGHT_JOIN`은 강한 도구지만, 고정된 조인 순서 의존은 장기적으로 부채가 될 수 있다.

- 데이터 분포가 변하면 과거 최적 플랜이 깨질 수 있음
- 통계/버전 변화에 취약할 수 있음
- 힌트가 누적되면 원인 분석보다 응급 처치가 습관화됨

즉, 당시 대응은 맞았지만 "최종 해법"은 아니다.

## 재현 근거

운영 DB에 다시 접근할 수 없기 때문에, 같은 질문을 재현 코드로 검증한다.

- 대표 재현: [JoinHintReproJdbcRepository.java](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/case-003-join-hint-repro/src/main/java/com/yowon/psc/case003/repro/infra/JoinHintReproJdbcRepository.java)
- 통합 테스트: [Case003JoinHintReproIntegrationTest.java](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/case-003-join-hint-repro/src/test/java/com/yowon/psc/case003/integration/Case003JoinHintReproIntegrationTest.java)

검증 명령:

```bash
cd /Users/yowon/study/til/TIL/problem-solving-cases
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
```

## 이 글의 한계

- 당시 운영 환경의 절대 성능 개선폭(P95/P99, CPU)은 확정할 수 없다.
- 이 글은 성과 보고서가 아니라, 의사결정과 트레이드오프의 회고다.

## 다음 글

다음 편에서는 "왜 이런 조인 변동성이 생겼는지"를 구조 관점에서 설명한다.

- [원인편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-02-root-cause-structure.md)
