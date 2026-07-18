# 04 — 테스트는 왜 그렇게 설계됐나

> `BaseClientThreadLocalLeakTest`의 4가지 핵심 결정의 근거를 정량적으로 분석한다.

---

## 1. 테스트 코드 한눈에

```java
@Test
@DisplayName("per-request new BaseClient를 1000번 반복해도 ThreadLocalMap Entry가 누적되지 않아야 한다")
void shouldNotLeakThreadLocalEntries_afterManyRequests() throws Throwable {
    final int iterations = 1000;                              // ① 1000회
    final AtomicInteger beforeCount = new AtomicInteger();
    final AtomicInteger afterCount = new AtomicInteger();
    final AtomicReference<Throwable> error = new AtomicReference<>();

    Thread testThread = new Thread(() -> {                    // ② 격리 스레드
        try {
            new StubBaseClient(null).request(...warmup...);   // ③ warmup → baseline
            beforeCount.set(ThreadLocalInspector.countEntries());

            for (int i = 0; i < iterations; i++) {
                StubBaseClient client = new StubBaseClient(null);  // 매번 new
                client.request(...);
            }

            afterCount.set(ThreadLocalInspector.countEntries()); // ④ stale 포함
        } catch (Throwable t) { error.set(t); }
    }, "leak-test-thread");

    testThread.start();
    testThread.join();
    if (error.get() != null) throw error.get();

    int delta = afterCount.get() - beforeCount.get();
    assertThat(delta)
        .as("ThreadLocalMap Entry 증가량 (1000회 반복 기준)")
        .isLessThan(50);                                      // ⑤ delta < 50
}
```

5개 결정: **iterations=1000 / 격리 스레드 / warmup baseline / stale 포함 카운트 / delta<50**.

---

## 2. 결정 ① — 왜 1000회인가

후보:
- 100회 — 너무 적음
- 1,000회 — 채택
- 10,000회 — CI 부담
- 100,000회 — 비현실

### 정량적 근거 4가지

#### (1) 신호/노이즈 비

**핵심 공식:**

```
누수 신호 = iterations × BaseClient의 ThreadLocal field 수
```

BaseClient 코드:

```java
public abstract class BaseClient<RequestClient, Response> {
    private final ThreadLocal<RequestClient> requestClient = new ThreadLocal<>();
    private final ThreadLocal<Response> response = new ThreadLocal<>();
    private final ThreadLocal<Boolean> responseSuccess = ThreadLocal.withInitial(() -> false);
    //                                                              ↑ 3개
}
```

per-request new BaseClient 패턴 → 매 호출마다 ThreadLocal 객체 3개 새로 생성 → ThreadLocalMap 에 entry 3개 박힘 → `set(null)` 만 하고 `remove()` 안 함 → entry 살아있음.

**계산표:**

| iterations | 이론 신호 (N × 3) | 실측 delta (case-006 lab) |
|---|---|---|
| 100 | 300 | (예상 ~300) |
| **1,000** | **3,000** | **3,000 ✅ 이론 상한 정확 일치** |
| 10,000 | 30,000 | (예상 ~30,000) |

→ 1,000회 실측 delta=3,000 = 이론 상한과 정확 일치. 즉 **자동 정리(cleanSomeSlots) 가 사실상 작동 안 함**. 새 ThreadLocal 객체마다 identityHashCode 가 분산돼서 stale 슬롯에 거의 안 닿음.

**baseline 노이즈** (case-006 lab 실측):

- BugClient warmup 후: **10 entry**
- FixA / FixB warmup 후: **4 entry**
- → 실제 노이즈 **4~10 entry**

**신호/노이즈 비:**

- 1,000회: 신호 3,000 / 노이즈 4~10 = **300~750배 마진**
- 임계값 50 적용: 노이즈 위 5~12배 ↑, 신호 아래 1/60 ↓
- → 누수 vs 정상 판정에 충분히 안전한 마진

#### (2) 운영 누수와 같은 자릿수

운영 MAT: **9,994 stale entry**
테스트: 1,000회 → 1,000~3,000 entry

→ **운영의 1/10 스케일**. 같은 자릿수에서 빠르게 검증. 100회면 운영 대비 1/100 → 너무 작아서 누수 신호가 노이즈에 묻힐 수 있음.

#### (3) ThreadLocalMap 자동 정리 임계값 통과

ThreadLocalMap.threshold = `len * 2 / 3`. 초기 capacity 16 → threshold 10. 10 넘으면 rehash.

`cleanSomeSlots` 가 rehash마다 stale entry 일부 정리하므로, **너무 적은 반복은 자동 정리에 묻혀** 누수가 보이지 않을 수 있다.

1,000회면:
- 16 → 32 → 64 → 128 → 256 → 512 → 1024 → 2048 까지 rehash 7번
- 각 rehash마다 자동 정리 시도하지만, 매번 새 ThreadLocal이 등장해서 정리량 < 새 entry 등장량
- **누수가 명백히 자동 정리를 압도** → 신호 보장

#### (4) CI 실행 시간

stub client (실제 HTTP 안 함):
- 1,000회 ≈ **1~2초**
- 10,000회 ≈ 15초
- 100,000회 ≈ 2분

→ 1,000회는 **CI 빌드에 의미있게 매번 돌릴 수 있는 한계**. 더 많으면 PR 리뷰 시간 늘어남, 더 적으면 신뢰성 떨어짐.

### 결론

> 1,000회는 **신호/노이즈 60배 + 운영 1/10 스케일 + 자동 정리 압도 + CI 2초 이내** 4가지 조건을 동시에 만족하는 sweet spot.

---

## 3. 결정 ② — 왜 격리된 스레드에서 돌리나

```java
Thread testThread = new Thread(() -> { ... }, "leak-test-thread");
testThread.start();
testThread.join();
```

JUnit 테스트 러너 스레드는 **이미 수십 개의 ThreadLocal entry를 가지고 있다**:

```
Mockito InternalMockHandler                           [ThreadLocal]
AssertJ ThreadLocalTimeUnitConfiguration              [ThreadLocal]
JUnit5 ExtensionContextInternal                       [ThreadLocal]
Spring TransactionSynchronizationManager              [ThreadLocal × N]
SLF4J / Logback MDC                                   [ThreadLocal]
JFR runtime tracking                                  [ThreadLocal × 5+]
... 합 30~80개 (테스트 프레임워크 설정에 따라)
```

만약 테스트 러너 스레드에서 그대로 측정하면:
- `beforeCount` = 50 (프레임워크 노이즈)
- `afterCount` = 50 + 1000 (BaseClient 누수) + α (테스트 진행 중 프레임워크 ThreadLocal 변동)
- `delta` = 1000 + α (불안정)

문제는 **α의 변동성**:
- 같은 테스트 여러 번 돌리면 α가 -10 ~ +30 사이 흔들림
- 다른 테스트 다음에 이 테스트가 돌면 시작 baseline 자체가 달라짐
- → **flaky test**

격리 스레드를 새로 만들면:
- 시작 baseline ≈ JDK 내부 5~10개 (predictable)
- 테스트 동안 프레임워크 영향 0
- 종료 후 thread도 GC 됨

→ **결정론적, 재현 가능, flaky 없음**.

### 한 가지 더 — `setName("leak-test-thread")`

힙 덤프 분석할 때 의미있는 thread name이 있어야 MAT이 보여줄 때 식별 가능. JUnit 기본 thread name은 `"main"` 또는 `"ForkJoinPool.commonPool-worker-1"` 같은 일반명이라 분석 어려움.

---

## 4. 결정 ③ — Warmup → Baseline 측정

```java
new StubBaseClient(null).request(...warmup...);   // 1회 warmup
beforeCount.set(ThreadLocalInspector.countEntries());
```

JDK는 lazy 초기화로 ThreadLocal을 **첫 사용 시** 만든다:
- TimeZone 캐시 → `LocalDateTime.now()` 첫 호출 시
- JFR → 일부 알로케이션 추적 시
- URL parsing → `new URL(...)` 첫 호출 시

테스트 시작 직후 baseline 측정하면 entry가 5개일 수 있는데, 첫 request() 호출하면서 갑자기 12개로 늘어남. 이게 누수로 잘못 측정될 위험.

**warmup 1회**는:
1. JDK 내부 ThreadLocal을 먼저 모두 초기화시킴
2. 그 후 baseline 측정하면 안정된 값 확보
3. 1000회 루프에서 추가 등장하는 entry는 **순수 BaseClient 누수**만

→ before/after 차이가 BaseClient 책임만 반영하게 보장.

---

## 5. 결정 ④ — Stale entry까지 카운트

```java
int count = 0;
for (Object entry : table) {
    if (entry != null) count++;   // ★ entry.value=null 이어도 entry 자체 있으면 카운트
}
```

[02 문서](02-threadlocal-internals.md) 에서 보았듯, `set(null)` 버그는 **entry는 살아있고 value만 null**인 stale entry를 만든다.

만약 `entry != null && entry.value != null` 로 카운트하면:
- `set(null)` 후 측정 → entry 살아있지만 value=null → **카운트 안 됨**
- → **버그가 안 보임**, 테스트 통과해버림 (false negative)

`entry != null` 로만 카운트하면:
- stale entry도 카운트
- value=null 인 좀비 entry까지 추적
- → **진짜 누수 감지**

이게 ThreadLocalInspector를 리플렉션으로 작성한 이유. 표준 API (`ThreadLocal.get()` 같은) 로는 stale entry 못 봄.

---

## 6. 결정 ⑤ — 임계값 50

후보:
- 0 — 너무 엄격
- 10 — 노이즈에 의해 flaky
- 50 — 채택
- 100 — 너무 느슨

### 분석

| 시나리오 | 예상 delta | 50 임계값 결과 |
|---|---|---|
| 수정 전 (set(null)) | 1,000~3,000 | FAIL ✅ |
| 수정 후 (close에서 clear) | 0~5 | PASS ✅ |
| 미래 새 ThreadLocal 추가 (의도된) | 5~20 | PASS (마진 확보) |
| 자동 정리 누락 케이스 | 30~50 | borderline (조사 트리거) |
| 진짜 누수 (한 자리 수만 누적) | 1,000+ | FAIL ✅ |

→ **50은 "운영 누수 신호 명백히 잡고, 미래 변경 마진 확보, 노이즈 위에 있음"**.

### 0이 안 되는 이유

JDK lazy ThreadLocal이 1000회 루프 중에도 추가 초기화될 수 있음:
- 예: WebClient 첫 사용 시 `DefaultExchangeStrategiesBuilder` 가 ThreadLocal 초기화
- warmup으로 대부분 잡지만 100% 보장은 안 됨

→ 0은 flaky, 50은 안정.

### 100이 너무 느슨한 이유

만약 미래에 `BaseClient` 에 ThreadLocal 1개 더 추가됐는데 cleanup 누락하면:
- 1000회 × 1 = 1000 entry 누수
- 임계값 100이면 잡힘 (1000 > 100)
- 임계값 50도 잡힘 (1000 > 50)

근데 **부분 누수** (cleanSomeSlots 가 우연히 절반 정리하면) 500 entry:
- 임계값 100에선 FAIL
- 임계값 50에선 FAIL

**더 작은 부분 누수** (자동 정리가 90% 잡으면) 100 entry:
- 임계값 100이면 borderline
- 임계값 50이면 명백히 FAIL

→ **50이 더 민감하면서도 false positive 0**. 안전 마진 충분.

---

## 7. 부수 결정 — 정상/예외 경로 분리

```java
@Test
@DisplayName("request() 정상 완료 후 ThreadLocal 값이 모두 제거되어야 한다")
void shouldClearThreadLocals_afterSuccessfulRequest() { ... }

@Test
@DisplayName("request() 예외 발생 후에도 ThreadLocal 값이 모두 제거되어야 한다")
void shouldClearThreadLocals_afterExceptionInRequest() { ... }
```

운영 RND-771의 진짜 원인은 **catch 블록의 set(null)** 였다. 정상 경로만 테스트했으면 못 잡았을 것.

→ try-finally-throw 양쪽 경로를 별도 테스트로 분리. 회귀 안정성 ↑.

---

## 8. 안 한 결정 — 의도적 비채택

### 안 한 것 1: WebClient 통합 테스트
- 이유: WebClient + DefaultExchangeStrategies 통합은 **WebFlux 자체 책임**
- 우리 책임은 BaseClient의 ThreadLocal 생명주기
- → 테스트 격리 원칙 (Single Responsibility)

### 안 한 것 2: 힙 덤프 자동 분석
- MAT 자동화는 가능하지만 너무 무거움
- 1000회 반복 + entry 카운트 만으로 **메커니즘 검증 충분**
- 힙 덤프는 운영에서 한 번 뜬 거로 충분

### 안 한 것 3: 멀티스레드 동시 요청 테스트
- ThreadLocal은 스레드별 격리되므로, 한 스레드 누수가 다른 스레드 영향 없음
- 단일 스레드에서 누수 메커니즘 검증되면 충분
- (case-006 lab에서는 추가 ConcurrencyTest로 보강 예정)

---

## 9. 결정 매트릭스 (요약)

| 결정 | 값 | 근거 (한 줄) |
|---|---|---|
| iterations | 1,000 | 신호/노이즈 60배 + 운영 1/10 + CI 2초 |
| 격리 스레드 | new Thread() | 테스트 러너 30~80 ThreadLocal 노이즈 회피 |
| warmup | 1회 | JDK lazy ThreadLocal 미리 초기화 |
| 카운트 방식 | stale 포함 | `set(null)` 버그가 stale entry 만듦 |
| 임계값 | < 50 | 노이즈 위 5배, 신호 아래 1/20 |
| 정상/예외 분리 | 2 테스트 | catch 블록 버그 직접 잡기 위해 |

---

## 10. 면접 답변 — 압축

> "회귀 테스트는 4가지 결정으로 설계했습니다.
> ① 1,000회 반복은 운영 누수 9,994의 1/10 스케일에서 신호/노이즈 비 60배로 강건한 검증을 위해.
> ② 격리된 새 Thread에서 돌려서 JUnit/Mockito 테스트 러너의 30~80 ThreadLocal 노이즈를 배제.
> ③ ThreadLocalMap 에 리플렉션으로 직접 접근해서 stale entry까지 카운트, set(null) 버그가 만드는 좀비 entry를 잡기 위해.
> ④ delta<50 임계값은 JDK 내부 lazy ThreadLocal 노이즈 위, 누수 신호 1000+ 한참 아래로 잡았습니다.
> 이 4가지 결정은 모두 ThreadLocalMap 내부 구현(linear probing + cleanSomeSlots amortized cleanup)을 정확히 이해해야 가능한 결정이었습니다."

---

## 11. case-006 lab 실측 검증 (2026-05-26)

이 4가지 결정을 적용한 회귀 테스트를 case-006 lab에서 실제 실행한 결과:

| 테스트 | 시나리오 | 측정 delta | 임계값 | 결과 |
|---|---|---|---|---|
| `Case006BugReproductionTest` | 1000회 + close 없음 | **3,000** | > 500 | ✅ FAIL 의도, 누수 명확 재현 |
| `Case006FixAStrategyTest` | 1000회 + finally clear | **0** | < 50 | ✅ 누수 0 |
| `Case006FixBStrategyTest` | 1000회 + try-with-resources | **0** | < 50 | ✅ 누수 0 (운영 채택안 검증) |

→ 임계값 50 기준의 신호/노이즈 분리가 **정확히 작동**. delta=3000 vs delta=0 — 60배 차이로 명백한 PASS/FAIL 구분.

→ 분석 문서 03번에서 이론적으로 예측한 "1000회 × 3 ThreadLocal = 3000 entry" 가 실측과 정확히 일치. **이론 상한이 실제로 발생** = `set(null)` 함정의 강력한 실증.

→ FixA / FixB 의 delta=0 은 임계값 50 한참 아래로, **미래 변경 마진** 충분히 확보됨을 보여줌.

---

## 12. 정리

- 4가지 의사결정 모두 ThreadLocalMap 내부 구현(linear probing + amortized cleanup) 이해 위에 세워짐
- case-006 lab 실측이 분석 예측과 정확 일치 → 이론 검증 완료
- 운영 RND-771 의 회귀 테스트가 같은 메커니즘을 보호한다는 확신 가능
