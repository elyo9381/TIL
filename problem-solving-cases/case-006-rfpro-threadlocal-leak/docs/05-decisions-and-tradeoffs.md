# 05 — 왜 이렇게 결정했나 (Decisions & Tradeoffs)

> 6개 결정 레벨을 STAR 구조로 한 곳에 모은 문서.
> 면접에서 "왜 그랬어요?" 질문이 어느 레벨로 들어와도 한 문서만 보면 답이 나오게 설계.

---

## 결정 트리 한눈에

```
[2025-10-23]              JsonClientWIthNewProxy AutoCloseable + close() 미리 도입 ✅
                                                ↓
Decision 1 [2026-02-26]   왜 ThreadLocal 도입?               → 멀티스레드 안전성, 시그니처 호환성
        ↓
Decision 2 [2026-02-26]   왜 set(null)?                       → 이전 instance field 의미를 표면 변환 (실수)
        ↓
        ❌ [2026-02-26]   close() 안에 BaseClient 3개 remove() 호출 누락 (실수)
        ↓
        [2026-03-05]      동료가 ThreadLocal 5개 추가 + close()에 5개 remove 추가
                          → 1 호출 = 8 entry / BaseClient 3개만 정리 누락 상태 유지
        ↓
        ─ 잠복 약 2.5개월 (운영 트래픽 누적) ─
        ↓
Decision 3 [2026-05-07]   힙덤프 → MAT 분석                  → 9,997 stale entry / 12,321 ExchangeStrategies / 1.15GB
        ↓
Decision 4 [2026-05-14]   왜 B 전략?                          → A는 getResponseBody 무효화, C는 비용 과다
        ↓
                          → close()에 super.clearRequestState() 한 줄 추가 (commit 29cfed0)
        ↓
Decision 5 [2026-05-14]   왜 1000회 반복 테스트?              → 검출 능력 포화 + CI 시간
        ↓
Decision 6 [2026-05-14]   왜 격리 스레드?                     → 테스트 러너 ThreadLocal 노이즈 회피
        ↓
Decision 7 [2026-05-14]   왜 stale entry 리플렉션 카운트?     → set(null) 버그가 만드는 좀비 entry 잡기 위해
```

> ⚠️ 주의: lab (case-006) 은 BaseClient의 3 ThreadLocal 만 모방.
> 운영 IrosInqClient 는 다단계 상속 (BaseClient → JsonClient → JsonClientWIthNewProxy → IrosInqClient) 으로 8 ThreadLocal entry per call.
> lab 은 누수 메커니즘 검증, 운영은 그 메커니즘이 8개 field 에 동시 작용한 multiplier.

---

## Decision 1 — 왜 ThreadLocal을 도입했는가 (2026-02-26)

### Situation
5년 6개월간 `BaseClient` 는 instance field 로 동작:
```java
@Getter private RequestClient requestClient;
@Getter private Response response;
@Getter private boolean responseSuccess = false;
```

문제 발생: 일부 서비스가 빈 풀링·공유 패턴으로 진화하면서 멀티스레드 동시 호출 가능성 등장. instance field는 race condition 위험.

### Task
멀티스레드 안전성 확보. 단, 17개 서비스가 의존하는 공통 라이브러리이므로 **시그니처 변경 0** 이 강한 제약.

### Options Considered

| 옵션 | 멀티스레드 안전 | 시그니처 변경 | 17 서비스 마이그레이션 | 채택? |
|---|---|---|---|---|
| A. 메서드 파라미터로 RequestContext 전달 | ✅ | ❌ 모든 abstract method 시그니처 변경 | 17개 동시 변경 | ❌ |
| B. Static ThreadLocal | ✅ | ✅ | 0 | ❌ (제네릭 타입 호환성 깨짐) |
| C. AutoCloseable + Instance ThreadLocal | ✅ | ⚠️ close 추가 | 17개 호출 측 점진 변경 | ❌ (당시 미검토) |
| **D. Instance ThreadLocal (no AutoCloseable)** | ✅ | ✅ | 0 | ✅ |

### Action
D 채택. commit `5e1290a`:

```diff
- @Getter private RequestClient requestClient;
- @Getter private Response response;
- @Getter private boolean responseSuccess = false;
+ private final ThreadLocal<RequestClient> requestClient = new ThreadLocal<>();
+ private final ThreadLocal<Response> response = new ThreadLocal<>();
+ private final ThreadLocal<Boolean> responseSuccess = ThreadLocal.withInitial(() -> false);
```

### Tradeoff (실제로 빠뜨린 한 줄)

**5개월 전 (2025-10-23) 본인이 이미 `AutoCloseable` + `close()` 패턴은 박아뒀음** (commit 3eb0e0bf). cleanup 강제 인터페이스 자체는 존재했음.

문제는 2026-02-26 BaseClient ThreadLocal 3개 도입할 때, **이미 있던 close() 안에 그 3개의 remove() 호출을 추가하지 않은 것**. catch 블록은 `set(null)` (remove와 다름) 으로 처리.

→ 동료가 7일 후 ThreadLocal 5개 추가했을 때 (2026-03-05, kyungnan) close()에 그 5개 remove 는 같이 추가했지만 BaseClient 3개는 여전히 빠진 채 → 1 호출 = 8 entry / 그 중 3개만 정리 안 됨 / 6주 잠복 후 OOM.

### 면접 답변 한 줄
> "5년 6개월간 instance field였던 BaseClient를 멀티스레드 안전성 확보 위해 ThreadLocal 로 전환했습니다. AutoCloseable + close() 패턴은 5개월 전 (2025-10-23) JsonClientWIthNewProxy 에 미리 도입해뒀는데, 정작 BaseClient 에 ThreadLocal 3개 도입할 때 (2026-02-26) 이미 있던 close() 안에 그 3개 remove() 호출 한 줄을 빼뜨린 게 RND-771 의 정확한 위치입니다."

---

## Decision 2 — 왜 set(null) 이었는가 (2026-02-26, 동일 commit)

### Situation
ThreadLocal 도입 시 catch 블록의 cleanup 패턴을 결정해야 함.
- 이전 instance field: `this.response = null;` (필드 비우기)
- 새 ThreadLocal: 어떻게 변환?

### Task
catch 블록의 의미("실패 시 상태 비우기")를 ThreadLocal에 맞게 옮기기.

### Options Considered (당시 의식적으로는 하나만 검토)

| 변환 | 의미 (인지) | 실제 동작 | 채택? |
|---|---|---|---|
| `this.response = null;` → `this.response.set(null);` | "값 비우기" 의미 동치 | Entry 살아있음, value만 null | ✅ (선택됨) |
| `this.response = null;` → `this.response.remove();` | (당시 검토 안 함) | Entry 자체 제거 | ❌ |

→ **`set(null)` 과 `remove()` 의 차이를 의식하지 못한 채** 표면 의미로 변환.

### Action (실수)
```diff
- this.response = null;
+ this.response.set(null);
```

### Why This Was a Mistake
ThreadLocal 의미론:
- `set(null)`: ThreadLocalMap 의 Entry는 그대로, value만 null
- `remove()`: Entry 자체를 ThreadLocalMap.table 에서 제거

→ Entry 살아있음 = key (`ThreadLocal` 객체) 가 BaseClient 인스턴스 필드에서 strong reference 로 잡혀 있음 = WeakReference cleanup 안 됨 = 누수.

### 면접 답변 한 줄
> "instance field 시절의 `this.response = null` 패턴을 `this.response.set(null)` 으로 표면 변환했는데, ThreadLocal 의미론에서 set(null) 은 Entry를 살려두고 value만 null로 만듭니다. remove() 와 다르다는 점을 그때 인지하지 못한 게 직접 원인이었습니다. 이건 instance field 와 ThreadLocal 을 같은 의미로 직관적으로 다루는 흔한 함정입니다."

---

## Decision 3 — 왜 B 전략(close 에서만 clear) 인가 (2026-05-14)

### Situation
ptspro-iros-inq 파드 OOM 반복 재시작 → 힙덤프 → MAT 분석:
- **DefaultExchangeStrategies 12,321 인스턴스 / retained 1.15GB / Old Gen 73% 점유**
- 16 컨슈머 스레드 ThreadLocalMap에 9,997 stale Entry
- 실제 IrosInqClient 의 상속 체인: BaseClient → JsonClient → JsonClientWIthNewProxy → IrosClientWithNewProxy → IrosInqClient
- 1 호출 = 8 ThreadLocal entry (BaseClient 3 + JsonClientWIthNewProxy 5)
- 그 중 BaseClient 3개만 cleanup 누락 (JsonClientWIthNewProxy 5개는 동료가 close()에 추가해놨음)

### Task
ThreadLocal 누수 차단 + `getResponse()`/`getResponseBody()` 호출 패턴 호환 + 17개 서비스 점진 마이그레이션.

### Options Considered

| 전략 | 누수 차단 | getResponse 정상 | 17 서비스 마이그레이션 | 채택? |
|---|---|---|---|---|
| **A. `request()` finally 에서 clearRequestState** | ✅ | ❌ request return 후 getResponse() = null | 17개 호출 패턴 변경 필요 | ❌ |
| **B. `close()` 에서만 clearRequestState (AutoCloseable)** | ✅ | ✅ try 블록 안에서 정상 | 17개 try-with-resources 추가 (점진) | ✅ |
| C. ThreadLocal 자체 제거 (메서드 로컬 변수) | ✅ | ✅ | 17개 동시 대규모 변경 | ❌ |

### A 전략 거부 사유 (구체)
```java
client.request(url, GET, data);
String body = client.getResponse();  // ❌ null !! (finally가 ThreadLocal 비웠음)
```
→ 운영 코드 광범위 의존 패턴 깨짐. 대량 회귀 위험.

### C 전략 거부 사유
- 17개 서비스의 `extends BaseClient` 서브클래스 동시 변경
- `onRequest()`, `onFailed()` 시그니처 변경 → 모든 사이트별 클라이언트 영향
- 코드 리뷰·QA 비용 폭발

### B 전략 채택 이유
- 호출 측 변경 = `try (Client c = new Client(...))` 한 줄 추가
- BaseClient 시그니처 변경 0
- 점진 마이그레이션 가능 (서비스마다 별개 PR)
- `clearRequestState()` 호출 시점이 **명시적 (close)** → 묵시적 약속에서 강제로

### Action
commit `29cfed0`:
```diff
+ protected void clearRequestState() {
+     this.requestClient.remove();
+     this.response.remove();
+     this.responseSuccess.remove();
+ }
```
+ 호출 측에서 `AutoCloseable` 구현 + try-with-resources

### 면접 답변 한 줄
> "A 전략(request finally clear) 검토했지만 운영 코드의 광범위한 getResponse() 호출 패턴이 null로 깨질 위험이 있었고, C 전략(ThreadLocal 제거) 은 17개 서비스 동시 변경 비용 과다였습니다. B 전략(close에서만 clear) 은 try-with-resources 한 줄 추가로 점진 마이그레이션 가능하면서 cleanup 시점이 명시적이 됩니다."

---

## Decision 4 — 왜 1,000회 반복인가 (2026-05-14)

### Situation
회귀 테스트 작성. 누수가 다시 발생하는 걸 자동 감지해야 함.

### Task
"누수 신호"가 명백히 잡히면서, 노이즈에 묻히지 않고, CI에서 매번 돌릴 수 있는 반복 횟수 결정.

### Constraints
- 운영 누수 규모: 12,321 DefaultExchangeStrategies / 1.15GB / Old Gen 73%
- 16 컨슈머 스레드 ThreadLocalMap 합쳐 9,997 stale Entry
- baseline 노이즈 (lab 실측): 4~10 entry
- CI 빌드 시간 영향: 5초 미만 권장
- lab의 BaseClient 모방 = 1 호출당 3 ThreadLocal entry (운영 8 entry 의 일부)

### Options Considered

| iterations | 이론 최대 누수 | 실측 delta (예상) | CI 시간 | 신호/노이즈 | 채택? |
|---|---|---|---|---|---|
| 100 | 300 | ~200 | 0.2s | 20배 | ❌ 자동 정리에 묻힐 가능성 |
| **1,000** | 3,000 | **3,000** (실측) | 1~2s | **60~300배** | ✅ |
| 10,000 | 30,000 | ~30,000 | 15s | 더 큼, but 변별력 추가 0 | ❌ CI 부담 |

### Action
1,000회 채택. 정직한 사전/사후 분리:

**사전 결정 근거 (1,000회 정한 시점)**:
1. 운영 자릿수 매칭 — 12,321 객체 / 1.15GB OOM 본 후, 같은 자릿수 검증 위해 N × 3 = 천 단위 신호 필요
2. CI 1~2초 안에 끝나는 실용적 한계

**사후 검증 근거 (lab에서 확인)**:
3. baseline 노이즈 4~10 실측 → 신호 3,000 / 노이즈 = **300배 마진** (검출 능력 포화)
4. 실측 delta=3,000 = 이론 상한 일치 → 자동 정리(cleanSomeSlots) 가 사실상 작동 안 함을 확인

### 실측 (case-006 lab)
```
[BugRepro] before=10, after=3010, delta=3000 (iterations=1000)
```
→ 이론 상한 정확 일치. 자동 정리가 거의 작동 안 함 (새 ThreadLocal 객체 identityHashCode 분산).

### 왜 10,000회 안 하는가? (FAQ — 면접 빈출)
| | 1,000회 | 10,000회 |
|---|---|---|
| 신호 | 3,000 | 30,000 |
| 노이즈 | 4~10 | 4~10 |
| S/N 비 | 300배 | 3,000배 |
| 검출 능력 | ✅ 포화 | ✅ 동일 |
| 변별력 추가 | — | **0** |
| CI 시간 | 1~2초 | 15초 |

→ S/N 300배에서 이미 누수 vs 정상 판정 정확도 포화. 더 늘려도 검출력 추가 0 (통계 power analysis 와 동일 논리). CI 시간만 15배.

### 면접 답변 한 줄
> "운영 누수는 DefaultExchangeStrategies 12,321 인스턴스가 1.15GB / Old Gen 73% 점유로 파드 OOM 반복 상태였습니다. 회귀 테스트 1,000회는 사전엔 운영 자릿수 매칭 + CI 시간 한계가 결정 근거였고, lab 사후 검증에서 신호 3,000 / 노이즈 4~10 = 300배 마진으로 검출 능력 포화 확인. 10,000회는 변별력 추가 0인데 CI 시간 15배라 거부 — 좋은 단위 테스트는 검출 능력 포화된 최소 횟수입니다. 운영 절대값 재현은 부하 테스트(k6) 의 책임."

---

## Decision 5 — 왜 격리 스레드인가 (2026-05-14)

### Situation
JUnit 테스트 러너 스레드는 자체 ThreadLocal 을 30~80개 가짐:
- Mockito InternalMockHandler
- AssertJ ThreadLocalTimeUnitConfiguration
- JUnit5 ExtensionContextInternal
- Spring TransactionSynchronizationManager
- SLF4J / Logback MDC
- JFR runtime tracking

### Task
누수 측정의 **결정론적 baseline** 확보.

### Options Considered

| 옵션 | baseline 노이즈 | flaky 위험 | 채택? |
|---|---|---|---|
| 테스트 러너 스레드에서 측정 | 50~80 | 높음 (테스트 순서·프레임워크 버전 영향) | ❌ |
| **새 Thread 격리 측정** | 5~10 (JDK lazy 만) | 낮음 | ✅ |

### A 옵션 거부 사유 (구체)
- 같은 테스트 여러 번 돌리면 baseline 변동 ±20
- 다른 테스트 다음에 이 테스트 돌면 baseline 자체가 달라짐
- → flaky test, CI 신뢰성 ↓

### Action
```java
Thread testThread = new Thread(() -> { ... }, "leak-test-thread");
testThread.start();
testThread.join();
```

부수 효과: thread name `"leak-test-thread"` 로 명시 → 힙 덤프 분석 시 식별 용이.

### 면접 답변 한 줄
> "JUnit 러너 스레드는 Mockito·AssertJ·JFR 등 30~80 ThreadLocal entry 를 가져서 baseline 변동이 ±20 정도 됩니다. 격리된 새 Thread 에서 돌리면 시작 baseline 이 JDK lazy 5~10 만 남아 결정론적이 되고, 종료 후 thread도 GC 됩니다. flaky test 회피 + CI 신뢰성 확보."

---

## Decision 6 — 왜 stale entry 까지 카운트인가 (2026-05-14)

### Situation
`set(null)` 버그는 **value만 null, Entry는 살아있는** stale entry 만듦.

### Task
이 stale entry 까지 잡아야 진짜 누수 감지.

### Options Considered

| 카운트 방식 | stale 감지 | API 표준 | 채택? |
|---|---|---|---|
| `ThreadLocal.get() != null` 체크 | ❌ value=null 이면 카운트 안됨 | ✅ public API | ❌ |
| **ThreadLocalMap 리플렉션 + `entry != null`** | ✅ entry 자체로 카운트 | ❌ JDK 내부 접근 | ✅ |

### 표준 API 거부 사유
- `ThreadLocal.get()` → value 반환, value=null 이어도 Entry 살아있을 수 있음
- `Thread.threadLocals` 는 package-private → java.lang 외부 접근 불가
- 즉 **표준 API로는 set(null) 버그가 만든 좀비 entry 자체를 못 봄**

### 리플렉션 방식 채택 이유
```java
Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
threadLocalsField.setAccessible(true);
Object threadLocalMap = threadLocalsField.get(thread);
Field tableField = threadLocalMap.getClass().getDeclaredField("table");
tableField.setAccessible(true);
Object[] table = (Object[]) tableField.get(threadLocalMap);

int count = 0;
for (Object entry : table) {
    if (entry != null) count++;   // ← stale 포함 (value=null 이어도 entry 자체 카운트)
}
```

### 비용 (의식적으로 받아들임)
- JVM 옵션 `--add-opens=java.base/java.lang=ALL-UNNAMED` 필수
- JDK 내부 구현 변경 시 깨질 가능성
- 프로덕션 코드 금지 → 테스트 유틸로만 사용

### 면접 답변 한 줄
> "set(null) 버그는 value=null 이지만 Entry는 살아있는 stale entry를 만듭니다. 표준 ThreadLocal API (ThreadLocal.get) 로는 이 좀비 entry 자체를 볼 수 없어요. ThreadLocalMap.table 에 리플렉션으로 직접 접근해서 entry != null 만 카운트하면 stale 까지 포함됩니다. JDK 내부 접근이라 --add-opens 옵션 필요하고 프로덕션엔 금지, 테스트 유틸로만 사용합니다."

---

## 6 결정의 결합 — 왜 모두 일관되어야 하는가

각 결정이 별개로 합리적이어도, **결합되어야 의미** 가 있음:

```
Decision 1 (ThreadLocal 도입)
   + Decision 2 (set(null) 함정)
       = 잠재 누수 (2.5개월 잠복)

Decision 3 (B 전략 수정)
   + Decision 4 (1000회 반복)
   + Decision 5 (격리 스레드)
   + Decision 6 (stale 카운트)
       = 자동 회귀 보호 (delta=3000 vs 0 명백 구분)
```

→ **회귀 테스트 4가지 결정 (4·5·6·임계값50)** 이 모두 정확해야 RND-771 같은 누수가 다시 발생할 때 자동 감지 가능. 한 결정이라도 잘못되면 false negative.

---

## 7 면접 시뮬레이션 — Q&A 모음

### Q0. "어떤 누수였어요? 한 번 설명해주세요."
A:
> "BaseClient 추상 클래스를 17개 서비스가 다단계 상속하는 구조였고, JsonClientWIthNewProxy 라는 중간 클래스를 제가 2025-10-23에 AutoCloseable 구현으로 만들어뒀습니다. close() 시점에 ThreadLocal 정리하는 패턴 자체는 미리 박은 거죠.
>
> 그런데 2026-02-26에 BaseClient에 ThreadLocal 3개 도입할 때, 이미 있던 close() 안에 그 3개를 remove() 하는 코드를 빠뜨렸습니다. catch 블록은 set(null) 만 호출했는데 ThreadLocal 의미론에서 set(null)은 Entry를 살려두고 value만 null로 만드는 거라 remove()와 다릅니다.
>
> 7일 후 동료가 JsonClientWIthNewProxy에 ThreadLocal 5개를 추가하면서 close()에 그 5개 remove는 같이 박았지만, BaseClient 3개는 빠진 채. 1 호출 = 8 entry, 그 중 BaseClient 3개만 정리 안 되는 상태가 됐어요.
>
> ptspro-iros-inq 서비스 16 컨슈머에서 약 2.5개월 잠복 후 ThreadLocalMap 9,997 stale Entry / DefaultExchangeStrategies 12,321 인스턴스 / 1.15GB / Old Gen 73% 점유 → 파드 OOM 반복. 힙덤프 → MAT Reference Pattern 분석으로 누수 경로 추적.
>
> 수정 (commit 29cfed0)은 제가 만든 close() 안에 super.clearRequestState() 한 줄 추가."

### Q1. "왜 ThreadLocal 썼어요? 다른 방법 없어요?"
A: Decision 1 답변 + 4가지 옵션 비교 표.

### Q2. "set(null) 이 왜 문제예요? 그냥 비우는 거 아니에요?"
A: Decision 2 답변 + ThreadLocalMap 내부 구조 (Entry/WeakReference/strong value).

### Q3. "왜 close에서만 clear예요? request 끝나고 바로 정리하면 안 돼요?"
A: Decision 3 답변 + A 전략의 getResponseBody null 무효화 시연 (case-006 FixA 테스트로 보여줄 수 있음).

### Q4. "왜 1000회예요? 100회/10000회는 안 돼요?"
A: Decision 4 답변. 한 줄: "검출 능력은 1,000회에서 신호/노이즈 300배로 포화. 10,000회는 변별력 추가 0인데 CI 시간 15배."

### Q5. "왜 별도 스레드에서 돌려요? 일반 테스트로는 안 돼요?"
A: Decision 5 답변 + JUnit 러너 ThreadLocal 노이즈 30~80.

### Q6. "왜 리플렉션 써요? ThreadLocal.get() 으로 안 돼요?"
A: Decision 6 답변 + stale entry 정의 + JDK 17+ 모듈 시스템.

### Q7. "lab은 ThreadLocal 3개인데 운영은 8개라면서요? 왜 8개로 안 만들었어요?"
A:
> "메커니즘 검증이 목적이지 운영 시뮬레이션이 목적이 아니에요. 누수 메커니즘은 BaseClient 자체에 있고, 상속 체인의 추가 5개는 같은 메커니즘의 multiplier 일 뿐. 모방을 8개로 늘려도 같은 결론 나오니까 단순화로 메커니즘 명확화. 운영 절대값 재현은 부하 테스트(k6) 의 책임."

### Q8. "본인이 도입하고 본인이 수정한 사례인데, 다음에 같은 실수 안 하려면?"
A:
> "AutoCloseable 패턴 자체는 미리 박아뒀는데, 새 ThreadLocal 추가할 때 기존 close() 안에 cleanup 추가하는 걸 빼먹은 게 정확한 위치였어요. 그래서 학습:
>
> 1. ThreadLocal field **추가** PR은 반드시 같은 PR에서 cleanup (close/remove) 코드도 같이.
> 2. 코드 리뷰 체크리스트에 'set(null) vs remove() 차이' 명시.
> 3. ThreadLocalMap 자동 정리는 best-effort라는 인지 — 명시적 remove 항상 필요.
> 4. 비슷한 패턴 다른 ThreadLocal 사용처도 동일 점검 (RND-785 후속).
>
> 패턴 자체를 만든 사람이 가장 빠뜨리기 쉬운 부분 — 자기가 만들었으니 머릿속에 항상 있다고 착각하기 때문."

---

## 8. 정리

> 이 lab과 분석 문서들의 진짜 의의는 "어떻게 고쳤다" 가 아니라 "왜 그렇게 고쳤다 + 왜 그렇게 검증한다" 의 일관된 의사결정 체인.
>
> 면접에서 각 "왜?" 질문이 어느 레벨로 들어와도, 트레이드오프 + 거부한 옵션의 거부 이유 + 채택안의 한계 + 그 한계를 어떻게 보완했는지를 한 호흡에 답할 수 있어야 진짜 시니어 수준 답변.
