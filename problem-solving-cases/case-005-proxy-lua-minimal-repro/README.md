# case-005-proxy-lua-minimal-repro

## 목적

이 모듈은 운영 전체 프록시 구조를 재현하지 않습니다.
`왜 Lua + requestId가 필요했는가`를 설명하기 위해, borrow 정합성 핵심 경로만 최소화해서 재현합니다.

## 재현 장면

1. 동시 borrow
- V1: `first-fit + read-modify-write`
- V2: `Lua + SETNX + requestId`

2. retry / 부분 실패
- V1: write 후 응답 실패 시 orphan lease 가능
- V2: 같은 `requestId` 재시도 시 기존 결과 재사용

## 주요 클래스

- `ProxyPoolSeedSupport`
- `LegacyBorrowService`
- `LuaBorrowService`

## Lua 파일

- `src/main/resources/lua/borrow.lua`
- `src/main/resources/lua/return.lua`

## 테스트

```bash
cd problem-solving-cases
./gradlew :case-005-proxy-lua-minimal-repro:test --rerun-tasks
```

핵심 테스트:
- `Case005ConcurrentBorrowIntegrationTest`
- `Case005RetryConsistencyIntegrationTest`
