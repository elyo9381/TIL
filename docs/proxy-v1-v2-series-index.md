# Proxy V1 -> V2 시리즈 인덱스

## 문서 구성

1. [proxy-borrow-return-architecture.md](/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/proxy-borrow-return-architecture.md)
   - 내부 구조 비교 기준 문서
   - V1, V2, 과도기, single-writer 목표 구조 정리

2. [blog-01-why-proxy-v2-was-needed.md](/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md)
   - 왜 V2가 필요했는가
   - 순차적 선택(first-fit) 편중
   - 도메인 비인식 모델의 한계
   - 도메인 비인식과 1:1 바인딩 문제

3. [blog-02-how-proxy-v2-changed.md](/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md)
   - V2는 무엇을 바꿨는가
   - domain pool, 순환 선택, 할당용 순환 큐, 락 기반 보수 경로
   - 상태 키 분리, requestId, TTL, single-writer

4. [case-005-proxy-lua-minimal-repro](/Users/yowon/study/til/TIL-case004-minimal-v1-v2/problem-solving-cases/case-005-proxy-lua-minimal-repro)
   - 최소 재현 코드
   - V2 전체 구조 재현이 아니라 borrow write-path 정합성 보조 근거
