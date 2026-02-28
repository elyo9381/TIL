# Case 001 Hypothesis

## Hypothesis 1
`Reservation`의 다중 `List@OneToMany`(Bag)와 연관 탐색이 결합되어 N+1이 발생한다.

## Hypothesis 2
Fetch Join으로 한 번에 해결하려 하면 중복/페이징 제약으로 관리가 어려워진다.

## Hypothesis 3
MySQL 8 CTE + 집계를 사용하면 필요한 데이터셋을 먼저 확정한 뒤 결과를 조합해 SQL 횟수를 줄일 수 있다.

## Validation Plan
1. baseline API와 optimized API를 같은 파라미터로 호출
2. SQL 수, 평균/중앙/p95를 비교
3. 응답 필드 동일성 확인
