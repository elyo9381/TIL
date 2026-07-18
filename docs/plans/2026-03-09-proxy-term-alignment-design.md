# Proxy terminology alignment design

## Goal
Align public docs to describe the V2 Redis structure as:
- domain baseline pool (`pool:{domain}`)
- allocation rotation queue (`cache:proxies:{domain}`)
- lock-based conservative path (fallback from the allocation queue path)

## Why
`cache/fallback` overstates the design as a storage-tier cache. In code, both structures live in the same Redis and differ by role:
- `pool:{domain}` is sync-managed baseline state
- `cache:proxies:{domain}` is a disposable, rotational working queue for allocation

## Scope
Update these docs only:
- docs/blog-01-why-proxy-v2-was-needed.md
- docs/blog-02-how-proxy-v2-changed.md
- docs/proxy-borrow-return-architecture.md
- docs/proxy-v1-v2-series-index.md if wording references old terms

## Replacement direction
- Avoid: `cache/fallback`, `빠른 경로`, `느린 경로`
- Prefer:
  - `도메인별 기준 풀`
  - `할당용 순환 큐`
  - `락 기반 보수 경로`
  - `동기화와 할당이 서로 간섭하지 않도록 분리한 구조`

## Verification
- Search for legacy phrasing after edits
- Read updated sections to ensure consistent wording
