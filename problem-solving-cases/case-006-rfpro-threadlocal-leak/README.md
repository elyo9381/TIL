# case-006-rfpro-threadlocal-leak

## 목적

운영 ThreadLocal 누수 사례(RND-771: `BaseClient` ThreadLocal 1.15GB 누수, `DefaultExchangeStrategies` 12,321개 / Old Gen 73%)를 **회사 코드와 완전히 격리된 환경**에서 재현하고, 다음 5가지 질문을 코드와 문서로 직접 검증한다.

1. **왜 ThreadLocal을 썼는가** → [`docs/01-why-threadlocal.md`](docs/01-why-threadlocal.md)
2. **ThreadLocal은 어떻게 구현되었는가** → [`docs/02-threadlocal-internals.md`](docs/02-threadlocal-internals.md)
3. **문제는 어떻게 발생했는가** → [`docs/03-leak-mechanism.md`](docs/03-leak-mechanism.md)
4. **회귀 방지 테스트는 왜 그렇게 설계됐는가** → [`docs/04-test-design-decisions.md`](docs/04-test-design-decisions.md)
5. **⭐ 6가지 결정의 "왜" 한곳에** → [`docs/05-decisions-and-tradeoffs.md`](docs/05-decisions-and-tradeoffs.md)
6. **Java Reference 4종 + ThreadLocalMap 비대칭 설계** → [`docs/06-references-and-design.md`](docs/06-references-and-design.md)
7. **Virtual Thread + ScopedValue + 2026 도입 현황** → [`docs/07-virtual-thread-and-scoped-value.md`](docs/07-virtual-thread-and-scoped-value.md)

이 모듈은 운영 코드를 그대로 복제하지 않는다. 메커니즘 재현 + 비교에 초점.

---

## 진짜 시간축 (git blame 검증)

| 날짜 | commit | 작성자 | 내용 |
|---|---|---|---|
| 2025-04-15 | `826bd45` | 본인 | JsonClientWIthNewProxy 최초 작성 |
| **2025-10-23** | `3eb0e0b` | **본인** | **AutoCloseable + close() 패턴 미리 도입** ⭐ |
| 2025-12-04 | `4c35998` | 본인 | ConnectionProvider 정리 패턴 추가 |
| **2026-02-26** | `5e1290a` | **본인** | **BaseClient에 ThreadLocal 3개 도입 — 이미 있던 close() 안에 cleanup 한 줄 누락** ❌ |
| 2026-03-05 | `0680588` | 동료 | JsonClientWIthNewProxy ThreadLocal 5개 추가 + close()에 5개 remove |
| 2026-05-07 | — | — | 운영 OOM 발현 → 힙덤프 → MAT 분석 |
| **2026-05-14** | `29cfed0` | **본인** | **RND-771 fix — close()에 super.clearRequestState() 한 줄 추가** |

→ 핵심: AutoCloseable 패턴 자체는 본인이 5개월 전에 미리 박았는데, BaseClient ThreadLocal 도입할 때 이미 있던 close() 안에 cleanup 코드 한 줄을 빠뜨린 게 RND-771의 정확한 위치.

---

## 운영 누수 핵심 숫자

```
운영 IrosInqClient (다단계 상속):
  BaseClient (3 ThreadLocal) → JsonClient → JsonClientWIthNewProxy (5 ThreadLocal) → IrosClientWithNewProxy → IrosInqClient
  → 1 호출 = 8 ThreadLocal entry 박힘
  → 그 중 BaseClient 3개만 cleanup 누락 (close()에 추가 안 됨)

ptspro-iros-inq 서비스 약 2.5개월 잠복 후:
  - 9,997 stale Entry (16 컨슈머 ThreadLocalMap 합)
    └─ 9,994 Response 가리킴 (BaseClient.response stale)
    └─ 3 WebClient 가리킴 (JsonClientWIthNewProxy.client 정상)
  - 12,321 DefaultExchangeStrategies 인스턴스
  - retained 1.15GB / Old Gen 73% 점유
  - 파드 OOM 반복 재시작
```

> ⚠️ lab(case-006)은 BaseClient의 3 ThreadLocal 만 모방. 누수 메커니즘은 BaseClient 자체에 있고 상속 체인 5개는 같은 메커니즘의 multiplier — 단순화로 메커니즘 명확화. 운영 절대값 재현은 부하 테스트(k6) 영역.

---

## 권장 학습 흐름

처음 보는 사람을 위한 7편 학습 순서:

```
06번 (Java Reference 4종 + 비대칭 설계)        ← 기초 개념
   ↓
02번 (JDK 21 소스 + table 자료구조 + HashMap 비교)   ← 코드 깊이
   ↓
01번 (왜 ThreadLocal 도입했나)                  ← 의사결정 히스토리
   ↓
03번 (누수 메커니즘 + 8 ThreadLocal 분포)        ← 본 사례 분석
   ↓
04번 (회귀 테스트 설계 + 검출 능력 포화)        ← 검증 의사결정
   ↓
05번 (6가지 결정 통합 + 면접 Q&A)               ← 면접 답변
   ↓
07번 (Virtual Thread + ScopedValue + 2026 현황) ← 시대 적응
```

→ 면접 직전이면 **05번 + 07번** 2개만 봐도 답변 골격 잡힘.

---

## 실행 결과 (2026-05-26 검증)

3 프로파일을 1,000회 반복 + 격리 스레드에서 측정한 결과:

| 프로파일 | 클래스 | before | after | delta | 임계값 | 결과 |
|---|---|---|---|---|---|---|
| `bug-set-null` | `BugClient` | 10 | 3,010 | **3,000** | > 500 (FAIL 의도) | ✅ 누수 재현 |
| `fix-a-strategy` | `FixAClient` | 4 | 4 | **0** | < 50 | ✅ 누수 0, getResponseBody null |
| `fix-b-strategy` | `FixBClient` | 4 | 4 | **0** | < 50 | ✅ 누수 0 + getResponseBody 정상 |

**6/6 테스트 PASS.** Bug delta=3000은 분석 문서 03번에서 예측한 "1000회 × 3 ThreadLocal = 최대 3000" 값과 정확히 일치 → 메커니즘 100% 재현.

```
[BugRepro] before=10, after=3010, delta=3000 (iterations=1000)
[FixA]     before=4, after=4, delta=0
[FixB]     before=4, after=4, delta=0
```

---

## 회사 코드 격리 원칙

| 격리 항목 | 방법 |
|---|---|
| 패키지명 | `com.example.http.base` (회사 패키지에서 변경) |
| 의존성 | 회사 라이브러리 의존 추가 안 함, `BaseClient.java` 단일 파일 복사 |
| 도메인 의존 | `EndpointConfig` 미니 버전 자체 작성 |
| 외부 API | 외부 의존 0, stub 응답 |
| 라이선스 | 원본 Apache 2.0 → 헤더 유지 + 패키지·작성자 정보 익명화 |

---

## 프로파일 (3가지 비교)

세 프로파일 공통 부모는 `BaseClient` 1개. close / catch / finally 동작만 서브클래스에서 override 해서 비교.

| 프로파일 | 클래스 | 운영 매핑 | 핵심 동작 |
|---|---|---|---|
| `bug-set-null` | `BugClient` | RND-771 직전 | `set(null)`만, close 없음 |
| `fix-a-strategy` | `FixAClient` | A 전략 (검토 후 폐기) | `request()` finally에서 `clearRequestState()` |
| `fix-b-strategy` | `FixBClient` | B 전략 (운영 채택, commit 29cfed0) | `AutoCloseable.close()` 에서 `clearRequestState()` |

---

## API

기본 prefix: `/api/case-006`, 기본 포트 `8086`

```bash
# Bug 패턴 (매 요청 새 BugClient → ThreadLocal 누수)
curl http://localhost:8086/api/case-006/bug

# Fix A 시도 (request finally clear → getResponseBody 무효화)
curl http://localhost:8086/api/case-006/fix-a

# Fix B (close에서 clear → 운영 채택안)
curl http://localhost:8086/api/case-006/fix-b

# 현재 스레드 ThreadLocal entry 카운트 + 힙 사용량
curl http://localhost:8086/api/case-006/stats
```

---

## 실행 방법

### 단위 테스트 (가장 신뢰성 높은 검증)

```bash
cd problem-solving-cases

# 전체 테스트 (6/6 PASS 확인)
./gradlew :case-006-rfpro-threadlocal-leak:test --rerun-tasks

# Bug 재현 테스트만 (delta>500 검증)
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006BugReproductionTest

# Fix A — 누수 0 + getResponseBody null 무효화 재현
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006FixAStrategyTest

# Fix B — 운영 채택안 검증
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006FixBStrategyTest
```

JVM 옵션 `--add-opens=java.base/java.lang=ALL-UNNAMED` 는 build.gradle.kts에 자동 적용됨 (ThreadLocalMap 리플렉션 접근).

### 애플리케이션 실행

```bash
./gradlew :case-006-rfpro-threadlocal-leak:bootRun
```

### 부하 테스트 (선택, k6 시나리오 구현 예정)

```bash
# 부하 발사 + 동시 stats 폴링
for i in {1..1000}; do curl -s localhost:8086/api/case-006/bug >/dev/null; done &
while true; do curl -s localhost:8086/api/case-006/stats | jq; sleep 1; done
```

---

## 디렉토리 구조

```
case-006-rfpro-threadlocal-leak/
├── README.md                                  ← 이 파일
├── build.gradle.kts                           # Java 21 + Spring Boot 3.4
├── .gitignore                                 # *.hprof, dumps/ 차단
├── docs/                                       ⭐ 분석 문서 7편 + plan
│   ├── 01-why-threadlocal.md                  # 5년 진화 + 본인 도입 사실
│   ├── 02-threadlocal-internals.md            # JDK 21 소스 + table 자료구조 + HashMap 비교 + rehash stall
│   ├── 03-leak-mechanism.md                   # 누수 메커니즘 (8 ThreadLocal + 12,321 분해)
│   ├── 04-test-design-decisions.md            # 테스트 의사결정 + 검출 능력 포화 논리
│   ├── 05-decisions-and-tradeoffs.md          # ⭐ 6가지 "왜" + 면접 Q&A 통합
│   ├── 06-references-and-design.md            # Java Reference 4종 + 비대칭 설계
│   ├── 07-virtual-thread-and-scoped-value.md  # ⭐ Virtual Thread + ScopedValue + 2026 도입 현황
│   └── plans/
│       └── 2026-05-26-case006-threadlocal-leak-design.md
├── src/main/java/com/til/case006/
│   ├── Case006Application.java
│   ├── client/
│   │   ├── BaseClient.java                    # http-base-lib 패키지 변경 복사
│   │   ├── EndpointConfig.java                # host 별 throttling/proxy/header 설정
│   │   ├── BugClient.java                     # 버그 상태
│   │   ├── FixAClient.java                    # A 전략
│   │   └── FixBClient.java                    # B 전략 (운영 채택)
│   ├── controller/
│   │   ├── BugController.java                 # /api/case-006/bug
│   │   ├── FixAController.java                # /api/case-006/fix-a
│   │   ├── FixBController.java                # /api/case-006/fix-b
│   │   └── InspectorController.java           # /api/case-006/stats
│   └── inspector/
│       └── ThreadLocalInspector.java          # JDK 리플렉션 진단 유틸
├── src/main/resources/
│   └── application.yml
├── src/test/java/com/til/case006/client/
│   ├── StubBaseClient.java                    # 테스트용 BaseClient stub
│   ├── Case006BugReproductionTest.java        # 1 테스트 — delta>500 검증
│   ├── Case006FixAStrategyTest.java           # 2 테스트 — 누수 0 + null 무효화
│   └── Case006FixBStrategyTest.java           # 3 테스트 — 누수 0 + 정상 + close
└── load-test/                                  ← (예약) k6 시나리오
```

---

## 재현 한계 (case-004 컨벤션)

- 운영의 16개 스레드 분산 누수가 아니라 단일 프로세스 누수
- WebClient 전체 통합 (`DefaultExchangeStrategies` 12,321개) 까지 가지 않고, 본질적인 ThreadLocalMap entry 증가만 재현
- 절대 성능 수치보다 **방향성·의사결정·트레이드오프** 검증에 초점
- MAT 분석은 별도 도구 (이 lab은 코드/테스트만)

---

## 참고 (회사 git 검증 가능, 본인 commit)

| 항목 | 값 |
|---|---|
| ThreadLocal 도입 commit | `5e1290a` (2026-02-26, "fix: BaseClient response/requestClient ThreadLocal 적용") |
| 누수 수정 commit | `29cfed0` (2026-05-14, "fix(RND-771): B전략 ThreadLocal 누수 수정") |
| 영향 서비스 | 17개 마이크로서비스 (회사 공통 HTTP 라이브러리 의존) |
| 운영 지표 | ThreadLocalMap entry 12,321 → 4 (고정), Old Gen 99% → 11~22% |

---

## 작업 상태

- [x] 디렉토리 골격 + settings.gradle.kts 등록
- [x] BaseClient / EndpointConfig / ThreadLocalInspector 패키지 변경 복사
- [x] 분석 문서 4편 + design plan
- [x] BugClient / FixAClient / FixBClient 구현
- [x] 컨트롤러 4개 (Bug / FixA / FixB / Inspector)
- [x] 단위 테스트 3개 클래스 / 6 테스트 (전부 PASS)
- [x] build 검증 (BUILD SUCCESSFUL, 6/6 PASS)
- [ ] k6 부하 시나리오 3개
- [ ] 블로그 글 1편
