# [재현편] Proxy V1 -> V2: Redis Lua로 정합성 이슈를 줄인 과정

## 1. 왜 이 글을 쓰는가

이 글은 "V1이 왜 흔들렸는지"와 "V2에서 무엇을 바꿨는지"를 감으로 설명하지 않고, 재현 코드로 검증하기 위해 작성했습니다.

목표는 성능 미화가 아니라, 아래 3가지를 같은 조건에서 비교하는 것입니다.

- 중복 할당(duplicate lease)
- 부분 실패 후 고아 lease(orphan lease)
- requestId 멱등성 불일치

## 2. V1에서 문제가 생기던 이유(요약)

V1은 read-modify-write 흐름이 분리되어 있고, 동시 요청이 겹치면 같은 리소스를 서로 가져갈 수 있는 창이 생깁니다.

또한 실패 시나리오에서 "쓰기 성공 + 응답 실패"가 나면 재시도 요청과 기존 상태가 어긋나 고아 상태가 남을 수 있습니다.

## 3. V2에서 바꾼 핵심

V2에서는 `borrow/return` 핵심 경로를 Lua 스크립트로 묶어 원자적으로 실행했습니다.

- `requestId`를 키로 멱등 처리
- lease 확보/검증/반납의 원자성 강화
- 실행 중 비교 리포트 간 간섭을 줄이기 위한 run lock 적용

핵심은 "빠른 코드"보다 "깨지지 않는 경로"를 우선한 것입니다.

## 4. 재현 구조

재현 모듈:

- [case-004-proxy-v1-v2-consistency](https://github.com/elyo9381/TIL/tree/master/problem-solving-cases/case-004-proxy-v1-v2-consistency)

비교 프로파일:

- `v1-p3`
- `v1-p4`
- `v2-target`

API:

- `GET /api/case-004/proxy/repro/compare`
- `GET /api/case-004/proxy/repro/report`

## 5. 검증 방법

```bash
cd problem-solving-cases
./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks
./gradlew :case-004-proxy-v1-v2-consistency:bootRun
curl -s "http://localhost:8080/api/case-004/proxy/repro/compare?domain=domain-a&concurrency=200&iterations=3&poolSize=10&appCount=1&cooldownMinutes=0" | jq
```

## 6. 이 글에서 강조하고 싶은 점

이번 개선의 본질은 "Lua를 썼다"가 아니라,

- 실패를 가정하고
- 경합을 재현해서
- 멱등성과 정합성을 지표로 확인한 뒤
- 개선을 반복했다는 점입니다.

## 7. 한계

- 운영 전체 플로우를 100% 복제한 코드는 아닙니다.
- 이 글의 범위는 "핵심 위험 경로 재현"과 "개선 패턴 검증"입니다.

절대 성능 수치보다, 정합성 리스크를 줄이는 설계 선택에 집중합니다.

