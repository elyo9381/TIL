# Proxy V1 -> V2 회고 인덱스

## 목적

V1 프록시 할당 구조에서 발생한 정합성 리스크를 V2에서 어떻게 줄였는지, 재현 가능한 코드 기준으로 정리한 문서 모음입니다.

## 문서

1. [재현편: Proxy V1 -> V2 정합성 비교](https://github.com/elyo9381/TIL/blob/master/problem-solving-cases/docs/blog-04-proxy-v1-v2-consistency.md)

## 코드 근거

- [case-004-proxy-v1-v2-consistency](https://github.com/elyo9381/TIL/tree/master/problem-solving-cases/case-004-proxy-v1-v2-consistency)

## 검증 명령

```bash
cd problem-solving-cases
./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks
./gradlew :case-004-proxy-v1-v2-consistency:bootRun
curl -s "http://localhost:8080/api/case-004/proxy/repro/compare?domain=domain-a&concurrency=200&iterations=3&poolSize=10&appCount=1&cooldownMinutes=0" | jq
```

