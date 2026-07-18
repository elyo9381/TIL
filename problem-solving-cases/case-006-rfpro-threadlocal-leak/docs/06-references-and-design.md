# 06 — Java Reference 4종 + ThreadLocalMap의 의도적 비대칭 설계

> 면접 빈출: "WeakReference 가 뭐예요?", "왜 ThreadLocalMap 이 그렇게 설계됐어요?"
> 이 문서 하나로 정확하게 답할 수 있게 정리.

---

## 0. 학습 목적

`02-threadlocal-internals.md` 가 ThreadLocal 의 코드 흐름을 다룬다면, 이 문서는 그 코드의 **설계 철학**을 다룬다:

- Java 의 4가지 Reference 종류 (Strong / Soft / Weak / Phantom)
- 왜 ThreadLocalMap 은 key=Weak, value=Strong 으로 의도적 비대칭 설계인가
- 그 비대칭이 RND-771 누수와 어떻게 직결되는가

---

## 1. Java Reference 4종 — 정확한 차이

### 1.1 한눈에

| 종류 | 클래스 | get() | GC 시점 | 주 사용처 |
|---|---|---|---|---|
| **Strong** | (일반 변수/필드) | ✅ 항상 | 참조 끊기 전까지 절대 안 됨 | 99% 일반 코드 |
| **Soft** | `SoftReference<T>` | ✅ 살아있으면 | 메모리 부족 시 (OOM 직전) | 캐시 |
| **Weak** | `WeakReference<T>` | ✅ 살아있으면 | **다음 GC 사이클 (메모리 여유 무관)** | **ThreadLocalMap key** |
| **Phantom** | `PhantomReference<T>` | ❌ 항상 null | GC 회수 직후 알림 | cleanup hook |

### 1.2 Strong Reference (강한 참조)

```java
ThreadLocal<String> t = new ThreadLocal<>();   // t 는 strong reference
List<String> list = new ArrayList<>();          // list 는 strong reference

// → t 와 list 가 살아있는 한 ThreadLocal/ArrayList 객체는 GC 안 됨
```

**특징**:
- 일반 변수, 필드, 메서드 파라미터, 컬렉션 element 모두 strong
- GC 가 절대로 회수 안 함 (참조가 끊기기 전까지)
- 명시적 `null` 대입, 변수 scope 종료, 또는 컬렉션에서 제거해야 회수 가능
- **메모리 누수의 일반적 원인** = 의도치 않은 strong reference 가 살아남는 것

### 1.3 Soft Reference (부드러운 참조)

```java
SoftReference<BigImage> ref = new SoftReference<>(loadBigImage());
BigImage img = ref.get();   // 살아있으면 반환, 회수됐으면 null
```

**특징**:
- 메모리 여유 있을 때는 strong 처럼 살림
- **OOM 직전에야** GC 가 회수 (즉 가능한 마지막까지 살림)
- 캐시에 적합 — 메모리 풍족하면 캐시 유지, 부족하면 자동 evict
- ThreadLocal 과는 무관

### 1.4 Weak Reference (약한 참조) ⭐ ThreadLocalMap 의 key

```java
ThreadLocal<?> t = new ThreadLocal<>();              // strong
WeakReference<ThreadLocal<?>> weakKey = new WeakReference<>(t);

System.out.println(weakKey.get());   // → ThreadLocal 객체

t = null;                            // strong reference 끊음
System.gc();                         // GC 트리거

System.out.println(weakKey.get());   // → null! ThreadLocal 객체 회수됨
```

**특징**:
- 다른 strong reference 없으면 **다음 GC 사이클에 무조건 회수** (메모리 여유와 무관)
- 객체의 lifecycle 을 외부 strong reference 에 위임
- 사용처: ThreadLocalMap.Entry, WeakHashMap, 캐시 일부, 이벤트 리스너

**핵심 동작 메서드**:
- `weakRef.get()` → 살아있으면 referent 반환, 아니면 null
- `weakRef.refersTo(obj)` → JDK 16+, reachability 영향 없이 identity 비교
- `weakRef.clear()` → referent 를 명시적으로 null 처리

### 1.5 Phantom Reference (유령 참조)

```java
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> ref = new PhantomReference<>(obj, queue);
ref.get();   // 항상 null
```

**특징**:
- `get()` 이 항상 null — 객체에 직접 접근 불가
- GC 가 회수한 직후 ReferenceQueue 에 enqueue 됨
- finalize() 대체용 cleanup hook
- ThreadLocal 과 무관

### 1.6 GC 회수 우선순위 (강한 → 약한)

```
Strong (절대 안 됨)
  ↓
Soft (메모리 부족 시)
  ↓
Weak (다음 GC 사이클)
  ↓
Phantom (회수됨 + 알림)
```

→ Weak 는 "**살아있을 명분이 없으면 즉시 회수**" 가 핵심.

---

## 2. ThreadLocalMap 의 의도적 비대칭 설계

### 2.1 Entry 클래스 (JDK 21 소스)

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);    // ← key (ThreadLocal 객체 자체) 는 WEAK reference
        value = v;   // ← value 는 일반 instance field = STRONG reference
    }
}
```

**핵심 비대칭**:

```
key   = WeakReference<ThreadLocal<?>>     ← 약한 참조
value = Object                            ← 강한 참조
```

이 비대칭은 우연이 아니라 **의도적 설계**.

### 2.2 왜 key 는 Weak Reference 인가?

**해결하려는 문제**: ThreadLocal 객체 자체의 회수.

```java
public void someMethod() {
    ThreadLocal<String> tempLocal = new ThreadLocal<>();
    tempLocal.set("hello");
    // 메서드 끝 → tempLocal 변수 사라짐
}
// → ThreadLocal 객체는 어떻게 회수되나?
```

만약 ThreadLocalMap 이 ThreadLocal 을 **strong reference 로** 잡았다면:
- 모든 thread 의 ThreadLocalMap 이 ThreadLocal 을 strong 으로 잡음
- 메서드 끝나도 ThreadLocal 회수 안 됨
- 매 메서드 호출마다 ThreadLocal 누적 → **더 큰 누수**

**Weak reference 해결**:
- 외부 strong reference (변수 `tempLocal`) 사라지면
- 다음 GC 시 ThreadLocal 객체 회수
- ThreadLocalMap.Entry 의 key 는 자동으로 null 됨 (= **stale entry**)
- 다음 set/get/remove 호출 시 자동 청소 트리거 (`cleanSomeSlots`, `expungeStaleEntry`)

→ **"ThreadLocal 객체가 외부에서 더 이상 안 쓰이면 자동 정리"** 가 weak key 의 목적.

### 2.3 왜 value 는 Strong Reference 인가?

**해결하려는 문제**: 사용자가 set 한 값의 의도치 않은 회수 방지.

```java
ThreadLocal<User> currentUser = new ThreadLocal<>();
currentUser.set(new User("alice"));

// User 객체는 currentUser ThreadLocal 안에서만 강하게 잡혀 있음
// 만약 value 가 weak 면? → 다른 strong reference 없으니 즉시 GC
// → currentUser.get() 했더니 null이 나오는 황당한 상황
```

**Strong value 해결**:
- 사용자가 명시적으로 `remove()` / `set(null)` / 다른 값으로 덮어쓰기 하기 전까지 value 보존
- value lifecycle = 사용자 책임 (예측 가능)

### 2.4 비대칭의 정상 시나리오

```
[ThreadLocal 객체에 대한 외부 strong reference 끊김]
        ↓
[다음 GC]
        ↓
ThreadLocal 객체 회수
        ↓
Entry.key = null (weak ref auto-clear)
        ↓
[다음 set/get/remove 호출]
        ↓
cleanSomeSlots / expungeStaleEntry
        ↓
Entry 자체 제거 + value 도 회수 가능
```

→ 의도대로 작동하면 자동 정리.

---

## 3. 비대칭이 만드는 함정 (RND-771 직접 연결)

### 3.1 instance field ThreadLocal 은 weak key 의 전제를 깬다

```java
public abstract class BaseClient {
    private final ThreadLocal<Response> response = new ThreadLocal<>();
    //            ↑
    //            BaseClient 인스턴스가 살아있는 한
    //            이 ThreadLocal 객체는 strong reference 로 잡혀 있음
    //            → weak key 자동 청소 영원히 트리거 안 됨
}
```

**시나리오**:

```
1. per-request new BaseClient()
        ↓
2. BaseClient 인스턴스 1 살아있음
        ↓
3. ThreadLocal<Response> 객체 1 → BaseClient.response 필드에 strong reference
        ↓
4. ThreadLocalMap.Entry.key (weak ref) 는 (3) 의 ThreadLocal 을 가리킴
   → BaseClient 인스턴스가 살아있는 한 ThreadLocal 도 살아있음
   → weak ref 가 회수될 명분 없음
        ↓
5. 다음 요청 → 또 new BaseClient()
        ↓
6. BaseClient 인스턴스 2 + ThreadLocal<Response> 객체 2 → 또 strong
        ↓
7. ThreadLocalMap 에 Entry 또 추가
        ↓
8. ... per-request × N 회 누적
```

→ **weak key 의 자동 cleanup 은 "ThreadLocal 객체 자체가 GC 됨" 이라는 전제 위에서만 작동**. instance field 패턴은 그 전제를 영원히 깸.

### 3.2 set(null) 의 추가 함정

```java
this.response.set(null);   // ❌
// → ThreadLocalMap 에서 Entry 자체는 살아있음
// → Entry.value 만 null 로 덮어씀
// → key (weak ref) 는 여전히 살아있는 ThreadLocal 가리킴
// → cleanSomeSlots 의 청소 대상이 될 수 있는 stale entry 가 안 만들어짐
//   (stale = key 가 GC 돼서 weak.get() == null 인 entry)
```

vs

```java
this.response.remove();    // ✅
// → ThreadLocalMap 의 Entry 자체를 제거
// → 강한 참조 (Entry.value) 끊어짐
// → value 도 GC 가능
```

### 3.3 결합 — RND-771 의 정확한 메커니즘

```
[설계 비대칭]
key=Weak, value=Strong
        ↓
[전제 위반 1] instance field ThreadLocal
ThreadLocal 객체가 BaseClient 필드로 strong 참조됨
→ weak key 자동 회수 영원히 안 됨
        ↓
[전제 위반 2] per-request new BaseClient
매 요청마다 새 ThreadLocal 객체 생성 + ThreadLocalMap entry 추가
→ entry 누적, 자동 청소 트리거 영원히 안 됨
        ↓
[전제 위반 3] set(null) 만 호출
Entry 자체는 살리고 value 만 null
→ stale entry 도 안 만들어져서 cleanSomeSlots 도 못 잡음
        ↓
[결과]
ThreadLocalMap 에 entry 무한 누적
+ value (Response, WebClient) 까지 strong 으로 살아있음
+ value 가 가진 다른 객체 (DefaultExchangeStrategies) 까지 chain reaction 으로 살아있음
        ↓
운영: 12,321 DefaultExchangeStrategies / 1.15GB / Old Gen 73% 점유 / OOM
```

---

## 4. 면접 답변 정리

### Q1. "WeakReference 가 뭐예요?"
> "다른 strong reference 가 없으면 다음 GC 사이클에 무조건 회수되는 참조입니다. SoftReference 처럼 메모리 여유를 보지 않고, 회수할 명분만 있으면 즉시 회수해요. ThreadLocalMap 의 Entry 가 ThreadLocal 객체를 weak 로 잡고 있어서, 외부에서 ThreadLocal 변수가 사라지면 자동으로 정리될 수 있게 설계됐습니다."

### Q2. "왜 ThreadLocalMap 은 key 가 Weak 이고 value 가 Strong 인가요? 같이 Weak 면 안 돼요?"
> "value 까지 weak 하면 사용자가 set 한 객체가 다른 곳에서 strong reference 가 없는 순간 즉시 GC 돼서 ThreadLocal.get() 이 null 나오는 일이 생깁니다. value 의 lifecycle 은 사용자 책임이라 strong 이어야 의미 있고, key 는 ThreadLocal 객체 자체가 외부에서 안 쓰이면 자동 정리되도록 weak — **의도적 비대칭**입니다."

### Q3. "그럼 이 설계가 왜 RND-771 누수를 일으켰어요?"
> "weak key 의 자동 cleanup 은 'ThreadLocal 객체 자체가 GC 됨' 을 전제하는데, BaseClient 의 instance field 로 ThreadLocal 을 잡으니까 BaseClient 인스턴스가 살아있는 한 ThreadLocal 도 살아있어요. per-request new BaseClient 패턴이면 매 요청마다 새 BaseClient + 새 ThreadLocal 생성, ThreadLocalMap 에 entry 누적, 자동 cleanup 영원히 트리거 안 됨. 거기에 catch 에서 set(null) 만 하니까 (remove 가 아니라) Entry 자체는 살아있고 value 도 strong 으로 잡혀 있어서 — 6주 잠복 후 12,321 객체 / 1.15GB OOM 으로 발현됐습니다."

### Q4. "Strong / Soft / Weak / Phantom 차이 한 줄로?"
> "Strong = 절대 회수 안 됨 (일반 변수). Soft = 메모리 부족 직전까지 살림 (캐시). Weak = 다음 GC에 무조건 회수 (ThreadLocalMap key). Phantom = 회수 직후 cleanup 알림 (finalize 대체)."

---

## 5. 핵심 한 줄

> **ThreadLocalMap 의 key=Weak / value=Strong 비대칭 설계는 "ThreadLocal 객체가 외부에서 안 쓰이면 자동 정리, 하지만 사용자가 set 한 값은 명시적으로 remove 할 때까지 보존" 의 균형. 이 균형은 ThreadLocal 객체 자체가 GC 가능하다는 전제 위에 있고, instance field 로 잡힌 ThreadLocal 은 그 전제를 영원히 깬다.**

---

## 6. 다음 학습

- 코드 흐름 — [`02-threadlocal-internals.md`](02-threadlocal-internals.md) (Entry 클래스 + set/get/remove + cleanSomeSlots)
- 누수 메커니즘 — [`03-leak-mechanism.md`](03-leak-mechanism.md) (8 ThreadLocal × per-request new 결합)
- 결정·트레이드오프 — [`05-decisions-and-tradeoffs.md`](05-decisions-and-tradeoffs.md)
