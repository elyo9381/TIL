# Case 001 Problem

## Context
- 대상: 예약 목록 API
- 관찰: 예약 20건 조회 시 연관 엔티티 접근으로 SQL이 폭증

## Reproduction Conditions
- Route 20개
- Route당 Schedule 3개
- Route당 Station 10개
- Reservation 20/2000건

## Symptom
- baseline API 호출 시 예약 수 대비 SQL 실행 수가 과도하게 증가
- 페이지당 응답시간 편차가 큼

## Goal
- 동일 응답 스키마를 유지하면서 SQL 수와 p95 응답시간을 줄인다.
