# problem-solving-cases

실무에서 겪은 이슈를 안전한 형태로 재현하고, 문제 원인과 개선 근거를 코드로 남기는 저장소입니다.

## Modules

- `common`: 공통 측정 유틸/설정
- `case-001-n1-cte`: N+1 + CTE 쿼리 최적화 재현
- `case-002-admin-style`: MOVV admin 조회 스타일(MyBatis + JSON 조합) 재현
- `case-003-join-hint-repro`: EXPLAIN 플랜 이탈과 `STRAIGHT_JOIN/FORCE INDEX` 대응 재현

## Prerequisites

- Java 11
- Docker / Docker Compose

## Quick Start (Docker MySQL)

```bash
cp .env.example .env
docker compose up -d
./gradlew :case-001-n1-cte:bootRun
```

`case-002-admin-style`은 기본적으로 `psc_case002` 데이터베이스를 사용합니다.

## Local MySQL Option

`.env` 또는 환경변수에서 `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`를 로컬 DB에 맞게 변경합니다.

## Case 001 API

- `GET /api/case-001/reservations/baseline?agno=10&limit=20`
- `GET /api/case-001/reservations/optimized?agno=10&limit=20`

## Case 002 API

- `GET /api/case-002/reservations/optimized?agno=10&offset=0&limit=20&locale=ko`

## Case 003 API

- `GET /api/case-003/join-hint/report?serviceType=VIP&limit=20`
- `GET /api/case-003/join-hint/benchmark?serviceType=VIP&limit=20&iterations=10`

## Case 002 Runbook

- `case-002-admin-style`는 MOVV admin형 스키마(`rvno`, `tkno`, `tb_ticket_route`, `tb_reserv_ticket.origin/dest JSON`)를 전제로 합니다.
- 런타임에서 스키마가 없으면 앱 시작 시 테이블을 자동 생성하고 seed를 적재합니다(`app.schema.auto-create=true`, `app.seed.enabled=true`).
- `tb_reserv`/`tb_pay` 등은 case-001과 컬럼 구조가 다르므로 `MYSQL_DATABASE_CASE002`로 별도 DB를 사용해야 합니다.

```bash
./gradlew :case-002-admin-style:bootRun
curl "http://localhost:8080/api/case-002/reservations/optimized?agno=10&offset=0&limit=20&locale=ko"
```

이미 실행 중인 mysql volume에 `psc_case002`가 없다면 1회 생성:

```bash
docker exec -i psc-mysql8 mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS psc_case002; GRANT ALL PRIVILEGES ON psc_case002.* TO 'psc'@'%'; FLUSH PRIVILEGES;"
```

테스트 기반 검증:

```bash
./gradlew :case-002-admin-style:test --tests "*Case002ReservQueryMapperIntegrationTest" --rerun-tasks
```

검증 포인트:
- `tb_pay` 다건 이력에서도 예약 row 중복이 생기지 않는지
- `payPrice` 집계가 합산값으로 나오는지
- `tb_ticket_route` 중복 row가 있어도 `itemDTOsJson`이 중복되지 않는지
- 실제 SQL 카운트가 요청 단위로 기록되는지

## Case 003 Runbook

`case-003-join-hint-repro`는 아래 시나리오를 코드로 재현합니다.

- CTE(`WITH ticket AS (...)`) 기반 조회에서 `EXPLAIN` 진입 테이블이 `tb_ticket_route(tr)`로 잡히는 케이스
- `STRAIGHT_JOIN + FORCE INDEX(idx_reserv_regdt)` 적용 시 `tb_reserv(r)`부터 타게팅하는 케이스

```bash
./gradlew :case-003-join-hint-repro:bootRun
curl "http://localhost:8080/api/case-003/join-hint/report?serviceType=VIP&limit=20"
curl "http://localhost:8080/api/case-003/join-hint/benchmark?serviceType=VIP&limit=20&iterations=10"
```

테스트 기반 검증:

```bash
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
```

검증 포인트:
- hint off/on의 `EXPLAIN` 첫 진입 테이블 차이(`tr` vs `r`)
- `Using temporary; Using filesort` 유무 차이
- `EXPLAIN FORMAT=JSON`의 `table_name` 차이(`\"tr\"` vs `\"r\"`)
- 응답 형태/조회 건수 비교 가능성
- 반복 실행 평균 지연(`withoutHint.avgLatencyMs`, `withHint.avgLatencyMs`) 비교 가능성

## Writing Draft

- `docs/blog-draft-movv-evolution.md`

## Verification

```bash
./gradlew clean test
./gradlew :case-001-n1-cte:bootRun
./gradlew :case-002-admin-style:bootRun
./gradlew :case-003-join-hint-repro:bootRun
./gradlew :case-002-admin-style:test --tests "*Case002ReservQueryMapperIntegrationTest" --rerun-tasks
./gradlew :case-003-join-hint-repro:test --tests "*Case003JoinHint*IntegrationTest" --rerun-tasks
```

실행 후 `case-001`은 두 API를 같은 파라미터로 호출해 실제 SQL 횟수와 응답시간을 비교합니다.
`case-001` metric은 요청 1회 스냅샷입니다(`sampleCount=1`, `p95LatencyMs=현재 요청 latency`).
