# [원인편] 왜 Reservation 조회에서 join 순서가 흔들렸나

> 이 글은 특정 회사의 내부 자산을 공개하지 않고, 구조적 원인을 일반화해 설명합니다.

## 한 줄 요약

문제의 본질은 "CTE를 써서"가 아니라, 다중 연관(Bag/EAGER), 고팬아웃 조인, 필터 적용 시점이 겹치며 실행계획이 흔들린 구조적 압력이다.

## 구조 압력 1: 엔티티/연관 축

Reservation aggregate에 컬렉션(`tickets`, `items`, `pays`)이 늘고, 일부는 EAGER였다.  
이 자체가 나쁘다는 뜻은 아니지만, 조회 시 조합 비용이 커질 가능성이 높아진다.

실무에서 자주 보이는 패턴은 다음과 같다.

- 도메인 요구가 늘어 aggregate가 확장됨
- 목록/통계 API가 함께 복잡해짐
- 조회 성능 이슈가 SQL/플랜 문제로 표면화됨

## 구조 압력 2: 쿼리 축

조회 쿼리는 결국 여러 요구를 동시에 만족해야 했다.

- 결제 합산/상태 노출
- 노선/정류장 연결
- 다국어/필터 조건

이런 상황에서 CTE는 "가독성 있는 중간 집합 분리"로 유용했다.  
하지만 CTE는 성능 보장 장치가 아니다. 최종 성능은 실행계획이 결정한다.

## 구조 압력 3: 플랜 축

문제를 키운 건 조인 진입점 변동성이다.

- `tb_ticket_route` 같은 고팬아웃 연결 테이블
- `service_type` 필터가 뒤쪽에서 확정되는 경로
- `LEFT JOIN` 연쇄로 중간 결과가 부풀 가능성

이 조합에서는 옵티마이저가 `tb_reserv`가 아니라 연결 테이블부터 타기도 한다.  
그러면 `Using temporary/filesort` 신호와 함께 불필요한 비용이 커질 수 있다.

## 그래서 STRAIGHT_JOIN이 왜 먹혔나

`STRAIGHT_JOIN`은 조인 순서를 강제해 "플랜 흔들림"을 바로 줄일 수 있다.  
즉시 효과가 있는 이유는 간단하다.

- 옵티마이저의 선택 폭을 줄여버리기 때문

하지만 이건 장기 해법이 아니라 단기 통제다. 데이터 분포가 바뀌면 역효과도 가능하다.

## 재현으로 확인한 포인트

`case-003`에서 같은 필터를 두 경로로 비교한다.

- hint off(CTE): 첫 진입 `tr`
- hint on(STRAIGHT_JOIN): 첫 진입 `r`

관련 코드/테스트:

- [JoinHintReproService.java](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/case-003-join-hint-repro/src/main/java/com/yowon/psc/case003/repro/application/JoinHintReproService.java)
- [Case003JoinHintApiIntegrationTest.java](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/case-003-join-hint-repro/src/test/java/com/yowon/psc/case003/integration/Case003JoinHintApiIntegrationTest.java)

## 결론

문제는 "개발자가 CTE를 잘못 이해해서"가 아니라,  
도메인 확장 + 조회 요구 증가 + 플랜 변동성이 누적된 결과다.

이 관점을 놓치면, 힌트 몇 줄의 성공/실패로만 사건을 해석하게 된다.

## 다음 글

다음 편에서는 "지금이라면 어떤 순서로 해결할지"를 실행 순서로 정리한다.

- [현재편](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-03-what-i-would-do-now.md)
