# Case005 Proxy Lua Minimal Repro Design

## Goal

블로그에서 설명할 수 있을 정도로 읽기 쉬운 최소 재현 코드를 별도 모듈로 만든다. 이 모듈은 운영 전체 구조가 아니라, `왜 Lua가 필요했는가`를 보여주는 두 장면만 다룬다.

## Scope

포함:
- V1 `first-fit + read-modify-write`
- V2 `Lua + requestId`
- 동시 borrow 장면
- retry / 부분 실패 장면
- Redis Testcontainers 기반 통합 테스트

제외:
- ProxyPool 생성 전체 흐름
- syncedProxyPool / pool:{domain} / cache:proxies:{domain}
- scheduler / cache / fallback
- API / controller

## Module Shape

새 모듈 이름은 `case-005-proxy-lua-minimal-repro`로 둔다.

구성 요소:
- `Case005ProxyLuaMinimalReproApplication`: 테스트용 Spring Boot 진입점
- `ProxyPoolSeedSupport`: Redis 키 초기화와 pool/resource seed 담당
- `LegacyBorrowService`: V1 borrow 경로
- `LuaBorrowService`: V2 borrow/return 경로
- `borrow.lua`, `return.lua`: V2의 원자적 write-path
- `RedisIntegrationTestSupport`: Testcontainers Redis 지원
- 테스트 2개:
  - `Case005ConcurrentBorrowIntegrationTest`
  - `Case005RetryConsistencyIntegrationTest`

## Redis Keys

V1:
- `repro:case005:v1:order`
- `repro:case005:v1:resource:{id}`

V2:
- `repro:case005:v2:pool`
- `repro:case005:v2:borrowed:{id}`
- `repro:case005:v2:lease:{id}`
- `repro:case005:v2:req:{requestId}`
- `repro:case005:v2:return:req:{requestId}`

## Test Story

### 1. Concurrent Borrow

- 같은 pool을 대상으로 동시 borrow를 건다.
- V1은 race window 때문에 중복 할당이 발생할 수 있다.
- V2는 Lua 안에서 `SETNX + lease write + request cache`를 묶어서 duplicate를 막는다.

### 2. Retry / Partial Failure

- write 후 응답 실패를 시뮬레이션한다.
- V1은 재시도 시 orphan 또는 request 결과 불일치가 생길 수 있다.
- V2는 `requestId`로 이미 처리된 요청 결과를 재사용한다.

## Why This Is Better Than Case004 For Blogging

- 파일 수가 적다.
- 목적이 명확하다.
- 운영 전체 구조를 흉내 내지 않으므로 과장 위험이 낮다.
- 블로그 본문에서 코드 1~2개만 골라 설명해도 된다.
