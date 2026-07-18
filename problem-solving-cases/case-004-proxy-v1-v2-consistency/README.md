# case-004-proxy-v1-v2-consistency

## 목적

`Proxy V1 -> V2` 전환에서 핵심이었던 정합성 이슈를, `v1-legacy`와 `v2-target` 두 경로만 남겨 비교하는 축약 재현 모듈입니다.

이 모듈은 운영 코드를 그대로 복제한 것이 아니라, 아래 두 장면을 검증하기 위한 실험 코드입니다.

- 동시 borrow에서 V1은 같은 자원을 중복 할당할 수 있는가?
- retry / 부분 실패에서 V1은 orphan / idempotency mismatch가 생기고, V2는 Lua + requestId로 이를 줄일 수 있는가?

## 프로파일

- `v1-legacy`: first-fit + read-modify-write 기반 legacy borrow 경로
- `v2-target`: Lua 기반 원자 처리 + requestId 멱등 제어

## 핵심 지표

- `duplicateLeaseCount`
- `orphanLeaseCount`
- `idempotencyMismatchCount`

## API

기본 prefix:

`/api/case-004/proxy/repro`

### 단일 프로파일 리포트

```bash
curl -s "http://localhost:8080/api/case-004/proxy/repro/report?profile=v1-legacy&domain=domain-a&concurrency=200&poolSize=10" | jq
curl -s "http://localhost:8080/api/case-004/proxy/repro/report?profile=v2-target&domain=domain-a&concurrency=200&poolSize=10" | jq
```

`profile` 값:

- `v1-legacy`
- `v2-target`

## 실행 방법

```bash
cd problem-solving-cases
./gradlew :case-004-proxy-v1-v2-consistency:bootRun
```

## 테스트

```bash
cd problem-solving-cases
./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks
```

## 재현 한계

- 실제 운영 분산 환경의 모든 경로를 다 포함하지는 않습니다.
- 이 모듈의 목표는 "증상 재현 + 개선 방향 비교"입니다.
- 성능 비교보다, 정합성 경로 차이를 확인하는 데 초점을 둡니다.
