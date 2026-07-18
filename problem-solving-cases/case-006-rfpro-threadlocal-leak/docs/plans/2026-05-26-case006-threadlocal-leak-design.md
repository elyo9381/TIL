# case-006-rfpro-threadlocal-leak — Design Plan

작성일: 2026-05-26
브랜치: `feature/case006-threadlocal-leak`
배경 티켓: RND-771 (운영 ThreadLocal 누수, 1.15GB / 73% Old Gen 점유)

---

## 1. 목적

운영에서 발생한 RND-771 ThreadLocal 누수를 **회사 코드와 완전히 격리된 환경**에서 재현하고, 다음 4가지 질문을 코드와 문서로 직접 검증한다.

1. **왜 ThreadLocal을 썼는가** — `BaseClient`의 설계 의도와 트레이드오프
2. **ThreadLocal은 어떻게 구현되었는가** — JDK 21 `ThreadLocal` / `ThreadLocalMap` 내부 구조
3. **문제는 어떻게 발생했는가** — `set(null)` vs `remove()` 메커니즘 + per-request `new BaseClient()` 패턴 결합
4. **회귀 방지 테스트는 왜 그렇게 설계됐는가** — 1000회 / 격리 스레드 / `delta < 50` 의 근거

---

## 2. 비목적 (Out of scope)

- 운영 전체 분산 환경 재현 (RabbitMQ, Redis, K8s 등)
- WebClient + 프록시 통합 시나리오 전부
- 운영 도메인 코드 (ExternalDataCollector*, ProxiedJsonClient 등) 그대로 복제
- 절대 성능 수치 (Old Gen %, GC pause 시간) 운영과 1:1 매칭

이 lab의 목표는 **메커니즘 재현 + 비교**다. 절대 수치보다 방향성·의사결정·트레이드오프 검증.

---

## 3. 재현 시나리오 (3 프로파일)

case-004의 `v1-p3 / v1-p4 / v2-target` 패턴, case-005의 `Legacy / Lua` 패턴과 동일한 방식.

| 프로파일 | 코드 | 운영 매핑 | 기대 결과 |
|---|---|---|---|
| `bug-set-null` | `BugClient`<br>catch에서 `set(null)`만, `clearRequestState()` 없음 | RND-771 직전 운영 코드 | OOM / Old Gen 99% / stale entry 선형 증가 |
| `fix-a-strategy` | `FixAClient`<br>`request()` finally에서 `clearRequestState()` 호출 | A 전략 (검토 후 폐기) | 누수 없음, **단 `getResponseBody()` null 무효화 문제** |
| `fix-b-strategy` | `FixBClient`<br>`close()` 에서만 `clearRequestState()` 호출, try-with-resources | B 전략 (운영 채택) | 누수 없음 + `getResponseBody()` 정상 동작 |

세 프로파일의 **공통 BaseClient는 1개**. catch / finally / close 동작만 서브클래스에서 override 해서 비교.

---

## 4. 핵심 지표

| 지표 | 측정 방식 | bug | fix-a | fix-b |
|---|---|---|---|---|
| `threadLocalEntryDelta` | `ThreadLocalInspector.countEntries()` before/after | ~1000+ | <50 | <50 |
| `getResponseBodyAfterRequest` | request() 후 `getResponse()` 반환값 | 정상 | **null (무효화 버그)** | 정상 |
| `oomReproduced` | -Xmx128m 기준 부하 | true | false | false |
| `clearRequestStateCallSite` | git grep | 없음 | request() finally | close() |

---

## 5. 디렉토리 구조

```
problem-solving-cases/case-006-rfpro-threadlocal-leak/
├── README.md                                      # 3 프로파일 + API + 실행
├── build.gradle.kts                               # Java 21, Spring Boot 3.4, WebFlux
├── src/main/java/com/til/case006/
│   ├── Case006Application.java
│   ├── client/
│   │   ├── BaseClient.java                        # http-base-lib에서 패키지 변경 복사 (Apache 2.0)
│   │   ├── BugClient.java                         # 버그 상태 재현
│   │   ├── FixAClient.java                        # A 전략
│   │   └── FixBClient.java                        # B 전략 (정답)
│   ├── controller/
│   │   ├── BugController.java                     # /api/case-006/bug
│   │   ├── FixAController.java                    # /api/case-006/fix-a
│   │   ├── FixBController.java                    # /api/case-006/fix-b
│   │   └── InspectorController.java               # /api/case-006/stats
│   ├── inspector/
│   │   └── ThreadLocalInspector.java              # http-base-lib에서 패키지 변경 복사
│   └── mock/
│       └── MockExternalServer.java                # WireMock embedded
├── src/main/resources/
│   ├── application.yml
│   └── logback-spring.xml
├── src/test/java/com/til/case006/
│   ├── Case006BugReproductionTest.java            # 1000회 → delta>1000 (FAIL 의도적)
│   ├── Case006FixAStrategyTest.java               # A 전략 + getResponseBody 무효화 재현
│   ├── Case006FixBStrategyTest.java               # B 전략 PASS
│   └── Case006ConcurrencyTest.java                # 동시 요청에서도 격리됨
└── load-test/
    ├── bug.js                                     # k6 - bug 엔드포인트
    ├── fix-a.js
    └── fix-b.js
```

---

## 6. 심층 분석 문서 (4편)

코드와 별도로, `docs/` 에 4개 분석 문서 작성. 면접·블로그 시드.

| 파일 | 내용 |
|---|---|
| `01-why-threadlocal.md` | BaseClient가 ThreadLocal을 쓴 이유, 설계 트레이드오프, 대안 설계와 비교 |
| `02-threadlocal-internals.md` | JDK 21 `ThreadLocal` / `ThreadLocalMap` / Entry / WeakReference / linear probing 정독 |
| `03-leak-mechanism.md` | per-request `new BaseClient()` + instance ThreadLocal + `set(null)` 결합으로 누수 발생 흐름. MAT 9,994 stale entry 추적 |
| `04-test-design-decisions.md` | 격리 스레드 / 1000회 / `delta<50` / stale entry 카운트 4가지 결정의 근거 |

---

## 7. 회사 코드 격리 원칙

| 격리 항목 | 방법 |
|---|---|
| 패키지명 | `com.example.http.base` → `com.til.case006.client` |
| 의존성 | `http-base-lib` 의존 추가 안 함, `BaseClient.java` 단일 파일 복사 |
| 도메인 의존 | `EndpointConfig`, `EndpointInfo` 미니 버전 자체 작성 |
| 외부 API | WireMock embedded, `httpbin.org` 등 외부 의존 0 |
| 라이선스 | http-base-lib가 Apache 2.0 → 헤더 유지하고 패키지 변경 복사 합법 |

---

## 8. 실행 방식

```bash
# Bug 재현 (OOM 발생까지 부하)
./gradlew :case-006-rfpro-threadlocal-leak:bootRun \
  --args='--spring.profiles.active=bug -Xmx128m'

# A 전략 검증 (getResponseBody 무효화 직접 재현)
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006FixAStrategyTest

# B 전략 검증 (운영 채택안 통과)
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006FixBStrategyTest

# 1000회 회귀 테스트 (운영 코드 미러)
./gradlew :case-006-rfpro-threadlocal-leak:test \
  --tests Case006ConcurrencyTest
```

---

## 9. 재현 한계 명시 (case-004 컨벤션)

- 운영의 16개 스레드 분산 누수가 아니라 단일 프로세스 누수
- WebClient 전체 통합 (DefaultExchangeStrategies 12,321개) 까지 가지 않고, 본질적인 ThreadLocalMap entry 증가만 재현
- 절대 성능 수치보다 **방향성·의사결정·트레이드오프** 검증에 초점
- MAT 분석은 별도 도구 (이 lab은 코드/테스트만)

---

## 10. 다음 액션

1. **Step 3**: 디렉토리 골격 + `settings.gradle.kts` 등록 + `build.gradle.kts`
2. **Step 4**: `BaseClient.java` + `ThreadLocalInspector.java` 패키지 변경 후 복사
3. **Step 5~8**: 4편 심층 분석 문서 작성 (코드 작성 전에 분석 먼저)
4. **Step 9**: README + 3 프로파일 컨트롤러/클라이언트 + 테스트
5. (선택) load-test k6 시나리오
6. (선택) 블로그 글

---

## 11. 작업 후 변경사항 (2026-05-26 업데이트)

이 plan 작성 후 실제로 진행되면서 결정된 사항:

- **WireMock 미채택**: 외부 HTTP 의존 0 정책 + Stub Response 만으로 충분히 메커니즘 재현 가능 → WireMock 의존성 제거
- **MockExternalServer.java 미작성**: 위와 동일 사유
- **logback-spring.xml 미작성**: Spring Boot 기본 설정 + application.yml 의 logging.level 만으로 충분
- **분석 문서 위치 이동**: `problem-solving-cases/docs/case006/` → `case-006-rfpro-threadlocal-leak/docs/` (case 자기완결 구조)
- **본인이 ThreadLocal 도입한 사실 발견**: git log 추적 중 commit `5e1290a` (2026-02-26) 가 본인 작성임을 확인 → 분석 문서 01번 전면 재작성, "외부 코드 분석" 톤에서 "본인 결정 책임 추적" 톤으로 변경

### 실측 결과 (lab 검증)

```
[BugRepro] before=10, after=3010, delta=3000 (iterations=1000)  ✅ 누수 재현
[FixA]     before=4, after=4, delta=0                            ✅ 누수 0
[FixB]     before=4, after=4, delta=0                            ✅ 누수 0 (운영 채택안)
```

→ 6/6 테스트 PASS, BUILD SUCCESSFUL.

---

## 부록: 본인이 한 일 정직한 표현

이 lab의 의의:

- 운영에서 진단·전략 결정·테스트 설계 → 본인이 한 사람의 일
- 회사 환경 의존 없이 동일한 메커니즘 재현 가능 → 본인이 그 메커니즘을 진짜 이해한다는 증거
- 면접에서 "이 lab 보세요"로 운영 코드 보여주지 않고도 깊이 입증 가능

면접 답변 골자:

> "5년 6개월간 instance field였던 BaseClient를 2026-02-26에 제가 ThreadLocal로 전환했습니다 (commit 5e1290a). 멀티스레드 안전 확보 목적이었지만 ThreadLocal 생명주기를 정확히 이해 못 한 채 catch 블록의 set(null) 패턴을 옮긴 것이 잠재 누수가 됐고, 2.5개월 후 운영에서 1.15GB Old Gen 점유로 발현됐습니다.
>
> MAT 힙 덤프에서 9,994개 stale entry를 직접 추적, A 전략(request finally clear)을 검토했지만 getResponseBody() 무효화 위험으로 거부, B 전략(close에서만 clear)으로 commit 29cfed0에 수정했습니다.
>
> 회귀 테스트는 1,000회 반복 + 격리 스레드 + 리플렉션으로 stale entry 카운트하는 방식으로 설계했고, 같은 메커니즘을 case-006 lab으로 재현해서 회사 코드 노출 없이 검증 가능합니다 (delta=3000 vs 0)."