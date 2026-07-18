# 01 — 왜 ThreadLocal을 썼나

> `BaseClient`의 5년 진화 히스토리를 git log로 추적하고, **본인이 ThreadLocal을 도입한 이유와 그 결정의 결과**를 정직하게 분석한다.

---

## 0. 솔직한 발견 — "외부에서 받은 코드"가 아님

이 분석을 시작하면서 처음에는 `BaseClient`가 "4년 전 외부에서 작성된 코드의 문제를 본인이 발견하고 수정한" 사례로 보였다. git log를 추적하면서 사실이 명확해졌다.

**진짜 타임라인** (회사 git log 기준):

| 시점 | 작성자 | 변경 | 상태 |
|---|---|---|---|
| 2020-08-05 | (다른 시니어) | `BaseClient` 최초 작성 | **instance field** |
| 2020 ~ 2025 | (여러 컨트리뷰터) | 점진 확장 (XPath, JsonClient, requestGet/Post 등) | instance field 그대로 |
| 2025-04-15 | 본인 | `WihtNewProxy` 객체 추가 | instance field 그대로 |
| **2026-02-26** | **본인** | **ThreadLocal 도입 (commit 5e1290a)** | **instance field → ThreadLocal** |
| 2026-05-07 | 본인 | 운영 OOM 발생, MAT 분석으로 누수 발견 | (진단 단계) |
| **2026-05-14** | **본인** | **수정 (commit 29cfed0, B 전략)** | **clearRequestState + close** |

핵심:
- 5년간 `instance field` 였다.
- **본인이 2026-02-26에 ThreadLocal로 전환**했다.
- 그 결정이 2.5개월 후 1.15GB 누수로 발현됐다.
- 본인이 발견했고 본인이 수정했다.

→ 이 사례의 의미는 "남이 만든 버그를 진단했다"가 아니라 **"자기 결정의 결과를 끝까지 책임지고 추적·수정한 완전한 시간축"** 이다. 면접 답변도 이 정직한 버전이 더 강하다.

---

## 1. Instance field 시절 (2020-08 ~ 2026-02, 5년 6개월)

`commit 5e1290a` 직전의 `BaseClient.java`:

```java
public abstract class BaseClient<RequestClient, Response> {
    @Getter private RequestClient requestClient;        // ← instance field
    @Getter private Response response;
    @Getter private boolean responseSuccess = false;

    public Response request(URL url, ...) {
        try {
            this.requestClient = createClient(url);
            this.response = onRequest(url, method, data, requestClient);
            this.responseSuccess = true;
            return this.response;

        } catch (Exception e) {
            this.requestClient = null;     // ← 평범한 instance field 클리어
            this.response = null;
            this.responseSuccess = false;
            onFailed();
            throw e;
        }
    }
}
```

이 시절의 특성:

- 단일 스레드 가정. 동일 `BaseClient` 인스턴스를 여러 스레드가 동시 호출하면 **race condition**.
- 운영 코드는 보통 **매 요청마다 `new SomeClient(...)`** 로 호출 — 인스턴스가 스레드 격리 역할 (per-request scope)
- 5년간 큰 사고 없이 운영. 단일 스레드 / per-request new 패턴이 race condition을 우회했음.

→ "왜 5년간 문제 없었나"의 답: **운영 코드가 instance field의 안전 가정(매 요청 new)을 지키고 있었기 때문**.

---

## 2. ThreadLocal 도입 결정 (2026-02-26, commit 5e1290a)

본인이 작성한 commit message:

```
fix: BaseClient response/requestClient ThreadLocal 적용 - 멀티스레드 안전성 확보

 BaseClient.java | 25 ++++++++++++----------
 1 file changed, 14 insertions(+), 11 deletions(-)
```

실제 변경:

```diff
- @Getter private RequestClient requestClient;
- @Getter private Response response;
- @Getter private boolean responseSuccess = false;
+ private final ThreadLocal<RequestClient> requestClient = new ThreadLocal<>();
+ private final ThreadLocal<Response> response = new ThreadLocal<>();
+ private final ThreadLocal<Boolean> responseSuccess = ThreadLocal.withInitial(() -> false);
+
+ public RequestClient getRequestClient() { return requestClient.get(); }
+ public Response getResponse() { return response.get(); }
+ public boolean isResponseSuccess() { return Boolean.TRUE.equals(responseSuccess.get()); }
```

```diff
- this.requestClient = requestClient;
- this.response = onRequest(url, method, data, requestClient);
- this.responseSuccess = true;
- return this.response;
+ this.requestClient.set(requestClient);
+ this.response.set(onRequest(url, method, data, requestClient));
+ this.responseSuccess.set(true);
+ return this.response.get();

  } catch (Exception e) {
-     this.requestClient = null;
-     this.response = null;
-     this.responseSuccess = false;
+     this.requestClient.set(null);     // ← 의미 보존: "값 비우기"
+     this.response.set(null);
+     this.responseSuccess.set(false);
      onFailed();
      throw e;
  }
```

### 왜 본인이 이 변경을 했는가

당시 정황 (2026-02-26 시점):
- 일부 외부 데이터 수집 서비스가 **풀링/공유 패턴**으로 진화 중 (단일 빈을 여러 스레드가 호출)
- RabbitMQ 멀티 컨슈머 환경에서 동일 `BaseClient` 인스턴스 동시 호출 가능성
- instance field 그대로면 race condition 위험

→ ThreadLocal 도입으로 **"인스턴스 공유해도 스레드별 격리"** 안전성 확보.

이 결정 자체는 합리적이었다. 멀티스레드 환경에서 ThreadLocal은 표준 패턴이다.

### 그러나 놓친 것 — ThreadLocal 생명주기

문제는 **catch 블록의 `set(null)`** 였다.

본인의 코드 변경 패턴을 보면:
- 이전 코드: `this.response = null;` (instance field 비우기)
- 새 코드: `this.response.set(null);` (ThreadLocal value를 null로)

**의미를 충실히 옮긴** 코드다. 그러나 ThreadLocal 의미론에서는 이 둘이 다르다:

| 동작 | instance field | ThreadLocal |
|---|---|---|
| `this.field = null` | 필드 값 = null. 비용 0. | (해당 없음) |
| `this.field.set(null)` | (해당 없음) | **Entry는 ThreadLocalMap에 그대로**, value만 null |
| `this.field.remove()` | (해당 없음) | Entry 자체 제거 |

→ instance field의 `null` 할당과 ThreadLocal의 `set(null)`은 **표면 의미는 같지만 메모리 동작이 완전히 다르다**.

본인은 이 차이를 정확히 인지하지 못한 채 변환했다.

### 더 놓친 것 — 정상 경로 cleanup 자체가 없음

이건 instance field 시절부터 있던 이슈인데, ThreadLocal로 바꾸니 폭발한 것.

- instance field 시절: 정상 경로에서 `requestClient`, `response`가 다음 요청 때 덮어쓰기 됨 → 별도 cleanup 불필요
- ThreadLocal 시절: 매 요청마다 **새 `BaseClient` 인스턴스**가 생성되면 매번 **새 ThreadLocal 객체**가 생성됨. 각각 ThreadLocalMap에 entry 박힘. cleanup 없으면 누적.

→ 이 차이를 인지하지 못한 채 ThreadLocal 전환이 이뤄졌다.

---

## 3. 4가지 후보 설계 비교 (당시 본인이 검토한 옵션)

본인이 ThreadLocal을 선택한 의사결정을 재구성해보면:

### 후보 A — 메서드 파라미터로 컨텍스트 전달

```java
public Response request(URL url, ..., RequestContext ctx) {
    ctx.client = createClient(url);
    ctx.response = onRequest(url, method, data, ctx);
    return ctx.response;
}
protected abstract Response onRequest(URL url, ..., RequestContext ctx);
protected abstract void onFailed(RequestContext ctx);
```

**장점**: ThreadLocal 없음, 누수 0
**단점**: 17개 서비스의 모든 `extends BaseClient` 서브클래스에서 시그니처 변경 필요. **breaking change**.

→ 비용 너무 큼. 거부.

### 후보 B — Static ThreadLocal (싱글턴 키)

```java
public abstract class BaseClient<R, S> {
    private static final ThreadLocal<RequestState> state = new ThreadLocal<>();
    // ...
}
```

**장점**: ThreadLocal 객체가 클래스당 1개. per-request `new BaseClient()` 해도 ThreadLocal은 재사용. entry 누적 0.
**단점**:
- 제네릭 `<R, S>` 와 static 필드 호환성 이슈 (static 컨텍스트에서 type parameter 사용 불가)
- 모든 `BaseClient` 서브클래스가 같은 ThreadLocal 공유 → 한 서브클래스의 cleanup 누락이 다른 서브클래스 영향

→ 제네릭 타입 시그니처 무너지면서 거부.

### 후보 C — Instance ThreadLocal + AutoCloseable 강제 (당시 검토 안 한 옵션)

```java
public abstract class BaseClient<R, S> implements AutoCloseable {
    private final ThreadLocal<R> requestClient = new ThreadLocal<>();
    // ...
    @Override
    public void close() {
        requestClient.remove();
        response.remove();
        responseSuccess.remove();
    }
}
```

**장점**: 호출 측이 try-with-resources 강제. cleanup 누락 컴파일 타임 경고.
**단점**: 17개 서비스 호출 측 변경 필요 (try-with-resources 추가). 단, 이건 점진 마이그레이션 가능.

→ **만약 이 옵션을 당시 검토했다면 RND-771 사전 차단 가능했다.** 검토 못 한 게 본인 책임.

### 후보 D — Instance ThreadLocal (본인 채택)

```java
public abstract class BaseClient<R, S> {
    private final ThreadLocal<R> requestClient = new ThreadLocal<>();
    // ...
    // close() 또는 cleanup 패턴 강제 없음
}
```

**장점**: 시그니처 변경 0, 호출 측 변경 0, 인터페이스 호환성 유지
**단점**: ThreadLocal 생명주기 관리가 **묵시적**. 누락 시 누수.

본인이 이걸 선택한 이유:
- **breaking change 회피** (17개 서비스 점진 변경 필요 없음)
- 즉시 멀티스레드 안전 확보
- 단순한 변경 (25라인 / 14삽입 / 11삭제)

→ 합리적 선택이지만 **생명주기 관리 정책을 누구도 강제하지 않는 구조**. 그게 누수 잠복의 토양이 됐다.

---

## 4. 도입 후 2.5개월의 침묵

`5e1290a` (2026-02-26) 머지 → 17개 서비스에 적용.

처음에는 문제 없어 보였다. 이유:
- 일부 서비스는 여전히 매 요청 `new BaseClient()` 패턴 (per-request scope)
- 일부 서비스는 빈 공유 패턴으로 진화 중 (멀티스레드 호출 시작)
- ThreadLocalMap 자동 정리 (`cleanSomeSlots`)가 초반 누수량 흡수
- Old Gen 천천히 차오르는 중 (운영 모니터링 알람 임계값 미달)

2.5개월 후, 특정 서비스(`ptspro-iros-inq`)의 RabbitMQ 컨슈머 16개에서 누적량이 **9,994 stale entry / 1.15GB / Old Gen 99%** 도달. OOM 알람 발생.

---

## 5. 발견 → 진단 → 수정 (2026-05-07 ~ 2026-05-14)

### 발견 (2026-05-07)

운영 파드 OOM 알람. 본인이 알람 받음.

### 진단 — Eclipse MAT (1주일)

```
Heap Dump (jcmd <pid> GC.heap_dump)
  → Eclipse MAT 로딩
  → Leak Suspects Report 자동 분석
  → 12,321 instances of DefaultExchangeStrategies (1.15GB, 73%)
  → Path to GC Roots → ThreadLocalMap → Entry → DefaultClientResponse
  → 9,994개 stale entry / 16 thread (스레드당 ~625)
```

→ **모든 누수가 ThreadLocal 한 군데로 수렴**.

### 코드 리뷰 — 본인이 본인 commit 다시 봄

`git blame` 으로 ThreadLocal 도입 commit 추적: `5e1290a`. **본인 작성**.
catch 블록의 `set(null)` 발견. **`set(null) ≠ remove()`** 임을 그제야 인지.

### 수정 전략 검토

A 전략 (`request()` finally) → `getResponseBody()` 무효화 위험 (호출 측 17개 서비스에서 의존)
B 전략 (`close()`만) → `AutoCloseable` 추가 + `ProxiedJsonClient` 등 호출 측에 try-with-resources 추가

→ **B 전략 채택**. 호출 측 변경 최소화하면서 생명주기 명시화.

### 수정 (2026-05-14, commit 29cfed0)

```diff
+ /**
+  * request() 완료 후 ThreadLocal 정리.
+  * 반드시 close() 또는 서브클래스의 종료 시점에서만 호출해야 한다.
+  * request() 내부의 finally에 넣으면 getResponseBody() 등 다음 호출이 null을 받는다.
+  * (RND-771: DefaultExchangeStrategies 1.15GB 누수 방지)
+  */
+ protected void clearRequestState() {
+     this.requestClient.remove();
+     this.response.remove();
+     this.responseSuccess.remove();
+ }
```

운영 결과:
- ThreadLocalMap entry 12,321 → **4 (고정)**
- Old Gen 99% → **11 ~ 22%**
- 추가 OOM 0건

---

## 6. 핵심 통찰

### 통찰 1 — 버그는 라인이 아니라 시간축이다

`set(null)` → `remove()` 한 줄 차이로 보이지만 진짜 원인은:

```
2020 설계: instance field BaseClient                      (5년 6개월간 안정)
+
2026-02 본인 변경: ThreadLocal 도입 (멀티스레드 안전 확보 의도)
+
운영 코드 진화: per-request new + 빈 공유 혼재
+
catch 블록: set(null) 의미를 instance field에서 ThreadLocal로 표면 변환
=
9,994 stale entry / 1.15GB 누수 (2.5개월 잠복 후 발현)
```

→ 어느 한 결정이 다르면 누수 안 났다. **결정의 결합 효과**.

### 통찰 2 — AutoCloseable 패턴은 미리 박았는데, cleanup 코드 한 줄 빼먹은 것

git blame 으로 검증된 진짜 시간축:

| 날짜 | 작성자 | 한 일 |
|---|---|---|
| 2025-04-15 | 본인 | JsonClientWIthNewProxy 최초 작성 |
| **2025-10-23** | **본인** | **AutoCloseable 구현 + close() 메서드 추가** ⭐ 패턴 미리 도입 |
| 2025-12-04 | 본인 | ConnectionProvider 정리 패턴 추가 |
| **2026-02-26** | **본인** | **BaseClient에 ThreadLocal 3개 도입 — 이미 있던 close() 안에 cleanup 코드 빠뜨림** ❌ |
| 2026-03-05 | 동료 | JsonClientWIthNewProxy에 ThreadLocal 5개 추가, close()에 5개 remove() 같이 추가 |
| 2026-05-14 | 본인 | RND-771 fix — close()에 `super.clearRequestState()` 한 줄 추가 |

**정확히 빠뜨린 것**:

5개월 전 (2025-10-23) 에 본인이 `AutoCloseable` + `close()` 패턴 자체는 미리 박아뒀음. 즉 cleanup 강제 인터페이스는 존재했음.

문제는 2026-02-26 에 BaseClient 에 ThreadLocal 3개를 도입할 때, **이미 있던 close() 메서드 안에 그 3개를 remove() 하는 코드를 넣지 않은 것**. 거기에 catch 블록은 `set(null)` (remove와 다름) 으로 처리.

→ "ThreadLocal API 자체 결함" 이 아니라, "AutoCloseable 패턴은 미리 만들어놨는데 새 ThreadLocal 추가할 때 그 패턴 안에 cleanup 한 줄 박는 걸 빼먹은 것" 이 정확한 위치.

**동료가 한 ThreadLocal 5개 추가 (2026-03-05) 는**:
- close() 안에 그 5개의 remove() 는 같이 추가 ✅ (본인이 만든 패턴 따라함)
- 하지만 BaseClient 의 3개는 여전히 cleanup 누락 (그 시점에도)

→ 결과: 1 호출 = 8 entry 박힘 / 그 중 BaseClient 3개만 정리 안 됨 / catch 시점에는 set(null) 만 호출 (Entry 살아있음).

→ 4년 후 학습: ThreadLocal field **추가할 때마다** 기존 close()/cleanup 메서드도 같이 업데이트 — 패턴 자체를 만든 사람이 가장 빠뜨리기 쉬운 부분 (자기가 만들었으니 머릿속에 항상 있다고 착각).

### 통찰 3 — 본인이 도입한 코드도 본인이 다시 봐야 한다

`git blame` 으로 본인 commit 추적했을 때 가장 빨리 원인 찾았다. 만약 다른 사람 코드라고 가정하고 진단했다면 더 오래 걸렸을 것.

→ **본인 commit이라고 면제 없다**. 모든 변경은 잠재적 회귀 원인.

---

## 7. 면접 답변 (정직한 버전)

> "5년 6개월간 instance field로 운영되던 BaseClient를 2026-02-26에 제가 ThreadLocal로 전환했습니다 (commit 5e1290a). 멀티스레드 환경 확장에 따른 race condition 회피 목적이었습니다. 그러나 ThreadLocal의 생명주기(set(null) ≠ remove())를 정확히 이해하지 못한 채 catch 블록의 기존 패턴을 그대로 옮긴 것이 잠재 누수가 됐고, 2.5개월 후 운영에서 1.15GB Old Gen 점유로 발현됐습니다.
>
> MAT 힙 덤프 분석으로 ThreadLocalMap → Entry → DefaultClientResponse → DefaultExchangeStrategies 9,994개 참조 체인을 직접 추적해서 원인 클래스가 본인 commit 5e1290a 임을 git blame으로 확인했습니다. A 전략(request() finally clearRequestState)을 검토했지만 getResponseBody() 무효화 위험으로 거부하고, B 전략(close()에서 clearRequestState)으로 commit 29cfed0에 수정했습니다.
>
> 같은 사람이 도입하고 발견하고 수정한 사례입니다. 이 과정에서 ThreadLocal 내부 구현(WeakReference key + strong reference value, expungeStaleEntry amortized cleanup의 한계)을 정확히 이해하게 됐고, 이후 ThreadLocal을 도입할 때는 반드시 AutoCloseable + try-with-resources로 생명주기를 인터페이스 레벨에서 강제하게 됐습니다."

---

## 8. 다음 문서

ThreadLocal이 JDK 내부에서 어떻게 구현되었기에 `set(null) ≠ remove()` 가 만들어지는지 → [`02-threadlocal-internals.md`](02-threadlocal-internals.md)
