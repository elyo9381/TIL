# Problem Solving Cases Design

**Date:** 2026-02-28
**Scope:** `problem-solving-cases` initial setup with `case-001-n1-cte`

## Goal
실무에서 겪은 N+1 + CTE 쿼리 최적화 경험을 공개 가능한 형태로 직접 재현하고, 코드 기반 근거를 통해 블로그 글 작성 품질을 높인다.

## Architecture
- 멀티모듈 Gradle 프로젝트
- `:common`: 측정/공통 설정/유틸
- `:case-001-n1-cte`: 재현 API, 엔티티, baseline/optimized 서비스
- MySQL 8 기본 실행(`docker-compose`) + 로컬 DB 옵션 문서 제공

## Data Model
- `Route`, `RouteSchedule`, `RouteStation`
- `Reservation`, `ReservationTicket`, `ReservationItem`, `Payment`
- `Reservation`에 `List` 기반 `@OneToMany` 3개(Bag)로 N+1 관찰 구조 확보

## Runtime Flow
1. baseline API: JPA 연관 접근으로 N+1 유도
2. optimized API: MySQL 8 CTE native query로 조회 횟수 축소
3. 동일 DTO 응답으로 결과 정합성과 성능을 함께 비교

## Metrics
- SQL statement count
- Response elapsed(ms)
- p95 latency(샘플 누적)
- 실행계획(EXPLAIN) 기록 포인트

## Deliverables
- 프로젝트 골격 + 실행 파일(`README`, `.env.example`, `docker-compose.yml`)
- `case-001` 문서 3종(`problem.md`, `hypothesis.md`, `validation.md`)
- baseline/optimized API + 테스트
