# Case 001 Validation

## Runbook
1. `docker compose up -d`
2. `./gradlew :case-001-n1-cte:bootRun`
3. baseline/optimized API를 각각 10회 호출

## Result Template

| Metric | Baseline | Optimized |
|---|---:|---:|
| SQL Count (avg) | - | - |
| Latency P50 (ms) | - | - |
| Latency P95 (ms) | - | - |

## Notes
- 결과가 크게 다르면 `EXPLAIN`으로 인덱스 사용 여부 확인
- 데이터 정합성(응답 건수, 합계, 라우트명)을 함께 비교
