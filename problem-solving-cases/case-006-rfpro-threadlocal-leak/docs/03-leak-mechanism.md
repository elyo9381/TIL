# 03 — 문제는 어떻게 발생했나

> per-request `new BaseClient()` + instance field ThreadLocal + cleanup 누락 + `set(null)` 결합으로 1.15GB 누수가 만들어진 메커니즘.

> **선행 문서**: [`01-why-threadlocal.md`](01-why-threadlocal.md) 에서 밝힌 진짜 히스토리 — 5개월 전 (2025-10-23) AutoCloseable 패턴은 본인이 미리 박았고, 2026-02-26에 BaseClient에 ThreadLocal 3개 도입할 때 이미 있던 close() 안에 cleanup 코드 한 줄을 빠뜨린 게 정확한 위치. 그 후 동료가 ThreadLocal 5개를 더 추가하면서 (2026-03-05) 누수 면적이 8개 entry per call 로 확장됨. 이 문서는 그 메커니즘이 JDK 내부 ThreadLocalMap 동작 + 운영 코드 패턴의 결합으로 어떻게 12,321 객체 / 1.15GB 까지 누적됐는지 추적한다.

---

## 1. 운영 MAT 분석 결과 (출발점)

### 1.1 메모리 점유

```
DefaultExchangeStrategies                    12,321 instances
                                             retained 1,153,112,328 bytes (1.15GB)
                                             73.19% Old Gen 점유
                                             → 파드 OOM 반복 재시작
```

### 1.2 참조 체인 (MAT Reference Pattern)

```
java.lang.Thread                              [16 스레드 — RabbitMQ 컨슈머]
  └─ ThreadLocalMap                           [16]
       └─ Entry[]                             [16]
            └─ Entry                          [9,997 stale]
                 ├─ → DefaultClientResponse   [9,994] ← BaseClient.response ThreadLocal
                 │     └─ DefaultExchangeStrategies (각 독립)
                 └─ → DefaultWebClient        [3 정상] ← BaseClient.requestClient
                       └─ DefaultExchangeStrategies (캐싱)

스레드당 평균: 9,997 / 16 ≈ 625 entry
```

### 1.3 운영 IrosInqClient 의 8 ThreadLocal 분포

```
IrosInqClient                                                 ThreadLocal 0개
  └ extends IrosClientWithNewProxy                            ThreadLocal 0개
      └ extends JsonClientWIthNewProxy                        ThreadLocal 5개  [동료 2026-03-05]
          ├─ lastRequestUrl    (ThreadLocal<URL>)
          ├─ actualProxyUsed   (ThreadLocal<Boolean>)
          ├─ actualProxyInfo   (ThreadLocal<String>)
          ├─ provider          (ThreadLocal<ConnectionProvider>)
          └─ client            (ThreadLocal<WebClient>)        ⬅ 큰 객체
              └ extends JsonClient                             ThreadLocal 0개
                  └ extends BaseClient                         ThreadLocal 3개  [본인 2026-02-26]
                      ├─ requestClient
                      ├─ response
                      └─ responseSuccess

→ 1 IrosInqClient 호출 = ThreadLocalMap에 8 entry 박힘
→ 그 중 BaseClient 3개만 cleanup 누락 (close()에 추가 안 됨)
→ JsonClientWIthNewProxy 5개는 close()에서 remove() 호출됨 (동료가 같이 박음)
```

### 1.4 12,321 vs 9,997 vs 9,994 의 정확한 분해

| 숫자 | 정확한 의미 |
|---|---|
| **16** | RabbitMQ 컨슈머 스레드 수 |
| **9,997** | 16 스레드 ThreadLocalMap 합쳐 stale Entry 총 수 |
| **9,994** | 그 중 `DefaultClientResponse` 가리키는 것 (BaseClient.response stale) |
| **3** | 그 중 `DefaultWebClient` 가리키는 것 (정상, JsonClientWIthNewProxy.client 살아있음) |
| **625** | 9,997 / 16, 스레드당 평균 |
| **12,321** | DefaultExchangeStrategies 인스턴스 전체 (1.15GB) |
| **차이 ~2,327** | ThreadLocal 외 다른 strong reference 경로 (Spring 캐싱 등) |

### 1.5 관찰 포인트

- 16개 스레드 (운영 RabbitMQ 컨슈머)
- 스레드당 ~625 stale entry → 모두 다른 ThreadLocal 객체 (per-request new BaseClient 패턴 증거)
- 각 stale Entry는 살아있는 Response/WebClient 를 strong reference 로 잡음
- 각 Response/WebClient 는 독립된 DefaultExchangeStrategies 를 strong reference

→ ThreadLocal 의 **WeakReference key** 가 살아있다는 뜻. 즉 **ThreadLocal 객체에 대한 강한 참조가 어딘가에 있다** (= BaseClient/JsonClientWIthNewProxy 의 instance field).

---

## 2. 운영 코드 패턴 — 설계 가정 위반

운영 서비스 (`ExternalDataCollectorService`) 흐름:

```java
@Service
public class ExternalDataCollectorService {
    @Autowired private SomeWebClientFactory factory;

    public void process(Request req) {
        // ⚠️ 매 요청마다 BaseClient 서브클래스 새로 생성
        ProxiedJsonClient client = new ProxiedJsonClient(req.getProxy());
        try {
            client.request(targetUrl, HttpMethod.POST, req.getData());
        } catch (Exception e) {
            // ...
        }
        // ❌ close() 안 부름. clearRequestState() 안 부름.
    }
}
```

여기서 **3가지 문제가 동시에 발생**:

### 문제 1 — instance field ThreadLocal × per-request new

`BaseClient`의 ThreadLocal 3개:

```java
public abstract class BaseClient<R, S> {
    private final ThreadLocal<R> requestClient = new ThreadLocal<>();
    private final ThreadLocal<S> response = new ThreadLocal<>();
    private final ThreadLocal<Boolean> responseSuccess = ThreadLocal.withInitial(() -> false);
    // ...
}
```

`new ProxiedJsonClient(...)` 가 호출될 때마다:
1. `BaseClient` 부모 생성자 호출
2. **3개의 ThreadLocal 객체가 새로 만들어짐**
3. 이 ThreadLocal 객체들은 `ProxiedJsonClient` 인스턴스 필드에 저장됨

→ 매 요청마다 **3개 새 ThreadLocal 객체**가 만들어지고, 각각 호출 스레드의 ThreadLocalMap에 entry를 박는다.

### 문제 2 — set(null)이 entry를 안 지움

`BaseClient.request()` 의 catch 블록:

```java
} catch (Exception e) {
    log.error("reqest ERROR!! url: {} | msg: {}", url, e.getMessage(), e);
    this.requestClient.set(null);   // ❌ remove()가 아님
    this.response.set(null);
    this.responseSuccess.set(false);
    onFailed();
    throw e;
}
```

[02 문서](02-threadlocal-internals.md)에서 본 바와 같이, `set(null)`은:
- ThreadLocalMap 에서 같은 key 찾음 (`e.refersTo(key)`)
- 매칭되면 `e.value = null` 만 함
- **Entry 자체는 그대로 살아있음**
- WeakReference key (ThreadLocal 객체)는 `ProxiedJsonClient` 인스턴스가 strong reference로 잡고 있어서 GC 안 됨
- → **stale entry로도 안 됨**. 그냥 살아있는 entry, value만 null

### 문제 3 — 정상 경로에서도 cleanup 없음

catch만 set(null)을 부르고, **정상 경로(try가 성공해서 return)는 ThreadLocal 정리 안 함**:

```java
try {
    this.requestClient.set(requestClient);
    this.response.set(onRequest(...));
    this.responseSuccess.set(true);
    return this.response.get();   // ← return 후 ThreadLocal 그대로
}
```

→ 정상 요청에서도 3개 ThreadLocal entry가 **그대로 살아있는 채로** 메서드 종료.

---

## 3. 한 요청에서 ThreadLocalMap에 무슨 일이 일어나는가

**시나리오**: 1번 요청 처리. 컨슈머 스레드 T1.

```
[T1.threadLocals = ThreadLocalMap]
                        │
                        ▼
[Entry[] table]    초기 entries: JFR 등 JDK 내부 5~10개
```

1. `new ProxiedJsonClient(proxy1)` 생성
   - 부모 BaseClient 생성자 → TL_A, TL_B, TL_C 3개 새 ThreadLocal 객체 생성
   - 이 시점엔 ThreadLocalMap에 entry 안 만들어짐 (set 호출 안 했으므로)

2. `client1.request(url, GET, data)` 호출 (정상 경로)
   - `TL_A.set(reqClient)` → `tab[hash(TL_A)] = new Entry(TL_A, reqClient)` ★ +1
   - `TL_B.set(response)` → `tab[hash(TL_B)] = new Entry(TL_B, response)` ★ +1
   - `TL_C.set(true)` → `tab[hash(TL_C)] = new Entry(TL_C, true)` ★ +1
   - return 후에도 entry 3개 그대로

3. 메서드 종료, `client1` 변수 스코프 끝
   - `client1` 객체에 대한 strong reference 사라짐 (Heap만 잡고 있음)
   - 하지만 **ThreadLocalMap의 Entry가 TL_A, TL_B, TL_C를 super(k)로 잡고 있음** — 단, weak reference라 자동 GC 가능 후보
   - 그러나 TL_A, TL_B, TL_C는 `client1.requestClient`, `client1.response`, `client1.responseSuccess` 필드에서 strong reference
   - **`client1` 자체가 살아있어야 strong reference가 유지됨**

4. GC 발생
   - `client1`이 reachable이 아니면 GC 됨 → TL_A/B/C도 strong reference 사라짐 → WeakReference key가 cleared
   - Entry는 stale 상태 (`e.refersTo(null)`)
   - 하지만 **Entry는 여전히 ThreadLocalMap.table에 남아있음** + `Entry.value` (실제 Response 객체)도 strong reference로 잡혀있음

→ value는 안 죽음. Response 객체 + 거기 매달린 DefaultExchangeStrategies (1MB) 그대로 살아있음.

---

## 4. 1000번 반복 = 1000~3000 누적 entry

각 요청에서 3개 ThreadLocal × 1000회 = **이론상 3000 entry**.

실제로는:
- `cleanSomeSlots` 가 새 entry 삽입 시 log2(n) 슬롯 검사 → 일부 stale entry 우연히 정리
- table.length가 16 → 32 → 64 → ... 로 rehash되며 일부 정리 기회
- 결과적으로 **1000~3000 사이 누적**

운영에서 9,994가 나온 이유:
- 16 스레드 × ~625 entry/스레드 = 9,994
- 각 스레드가 약 625번 요청 처리 후 누적

### case-006 lab 실측 결과 (2026-05-26 검증)

이 lab의 `Case006BugReproductionTest` 실행:

```
[BugRepro] before=10, after=3010, delta=3000 (iterations=1000)
```

**delta=3000 — 이론 상한과 정확 일치**. cleanSomeSlots 의 자동 정리가 거의 작동하지 않은 케이스. 이유 추정:
- 매 iteration마다 **새 ThreadLocal 객체 3개 생성**
- 새 객체의 `threadLocalHashCode` 가 기존 stale entry slot 과 충돌하지 않으면 자동 정리 안 일어남
- 1000회 동안 hash 충돌이 거의 0 → stale entry가 자기 슬롯에서 그대로 누적

→ **이론상 최악의 상한이 실제로 발생**. `set(null)` 의 함정이 단순한 이론이 아니라 측정 가능한 실측.

---

## 5. value 쪽 strong reference 체인

stale entry의 `Entry.value` 가 잡고 있는 객체 그래프:

```
Entry.value: DefaultClientResponse
  ↓ strong field
ClientResponse
  ↓ exchangeStrategies field
DefaultExchangeStrategiesBuilder$DefaultExchangeStrategies     ← 운영 12,321개
  ↓ readers, writers field (List<HttpMessageReader>, ...)
JacksonJsonDecoder, FormHttpMessageReader, ProtobufHttpMessageReader, ...
  ↓ 각 reader/writer가 ObjectMapper, HttpHeaders 캐시 등 보유
  ↓ 합 ~1MB / instance
```

→ **1.15GB / 9,994 ≈ 115KB per Response** (실제 측정)
→ 9,994 × 115KB = 1.15GB **(MAT 분석과 정확 일치)**

---

## 6. 자동 정리가 따라잡지 못한 이유

[02 문서](02-threadlocal-internals.md) `cleanSomeSlots` 분석에서:
- `set()` 마다 `log2(n)` 슬롯만 검사
- 16 → 32 → 64로 rehash되며 검사 범위도 늘어남
- 하지만 **stale entry가 hash 분산되어 있으면** 검사가 못 닿음

운영에서:
- 매 요청마다 **3개의 새 ThreadLocal 객체**
- 각각 다른 `threadLocalHashCode` (atomic counter 기반)
- → table 안에서 **위치가 골고루 분산**
- 자동 정리가 우연히 맞을 확률 낮음
- 누적되며 점점 dense해짐

---

## 7. 수정 전략 비교

운영 코드 흐름을 가정하면 수정안은 3가지 가능했다:

### A 전략 — request() finally에서 clear

```java
try {
    this.requestClient.set(...);
    this.response.set(onRequest(...));
    this.responseSuccess.set(true);
    return this.response.get();
} finally {
    clearRequestState();   // ← 매 request 끝에 정리
}
```

**장점**: 누수 즉시 해결, ThreadLocal 빠른 정리
**치명 단점**: `request()` 가 return 후 호출 측이 `getResponse()` 또는 `getResponseBody()` 호출하면 **null 반환**.

```java
client.request(url, GET, data);
// finally에서 remove() 됨
String body = client.getResponse();  // ← null! (운영에서 의존 코드 깨짐)
```

운영 코드에 `getResponse()`, `getResponseBody()` 호출 패턴이 광범위 존재. A 전략 적용 시 **17개 서비스 양쪽 동시 변경 필요**. 비현실적.

### B 전략 — close() 에서만 clear (운영 채택)

```java
public class ProxiedJsonClient extends BaseClient<...> implements AutoCloseable {
    @Override
    public void close() {
        super.clearRequestState();
    }
}

// 사용 측
try (var client = new ProxiedJsonClient(proxy)) {
    client.request(url, GET, data);
    String body = client.getResponse();   // ✅ 정상 (close 전이라 ThreadLocal 살아있음)
}
// try-with-resources 종료 → close() 자동 호출 → ThreadLocal 정리
```

**장점**:
- `getResponse()` 정상 동작 유지
- 호출 측에 try-with-resources만 추가하면 끝
- BaseClient 시그니처 변경 0
- 17개 서비스 점진 마이그레이션 가능

**단점**:
- 호출자가 try-with-resources 안 쓰면 또 누수
- → 그래서 **회귀 테스트 1000회 반복**으로 보호

### C 전략 — ThreadLocal 자체 제거 (가장 깨끗하지만 비현실)

`BaseClient`의 ThreadLocal을 메서드 로컬 변수 + 파라미터 전달로 변경. 17개 서비스 동시 마이그레이션 필요. 4년 전 했어야 할 일. 지금은 비용 너무 큼.

→ **B 전략 채택. 운영 commit 29cfed0 (2026-05-14)**.

---

## 8. 핵심 통찰 — "한 줄 버그가 아니다"

`set(null)` → `remove()` 한 줄 차이로 보이지만, 진짜 원인은 **3겹의 결합**:

```
설계 결정 1: ThreadLocal을 instance field로                (4년 전)
       +
설계 결정 2: catch에서 set(null)만 호출 (불완전 cleanup)    (4년 전)
       +
운영 코드 진화: per-request new BaseClient() 패턴 등장       (이후)
       =
9,994 stale entry / 1.15GB 누수
```

→ 어느 한 결정만 다르면 누수 안 났다:
- ThreadLocal이 static이었으면 → 매 요청 같은 ThreadLocal 객체 재사용, 누적 0
- catch에서 remove() 호출했으면 → entry 즉시 정리
- per-request new가 아니라 풀링했으면 → ThreadLocal entry 재사용

→ **버그 수정은 한 줄이지만, 진짜 학습은 4년 시간축의 결정 흐름**.

---

## 9. 다음 문서로

이 메커니즘을 자동으로 검증하는 회귀 테스트가 어떻게 설계됐는지 다음 문서(`04-test-design-decisions.md`)에서 본다.
