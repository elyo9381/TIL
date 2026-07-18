# 02 — ThreadLocal은 어떻게 구현되었나

> JDK 21 (`java.base/java/lang/ThreadLocal.java`, 30,225 bytes) 소스를 기반으로 한 내부 구현 정독.

> **선행 권장**: [`06-references-and-design.md`](06-references-and-design.md) 에서 Java Reference 4종 (Strong/Soft/Weak/Phantom) + ThreadLocalMap 의 key=Weak / value=Strong 의도적 비대칭 설계를 먼저 학습하면 이 문서의 코드 흐름이 더 명확해진다.

---

## 1. 전체 자료구조

```
┌─────────────────────────────────────────────────────────────────┐
│ Thread (java.lang.Thread)                                        │
│   ThreadLocal.ThreadLocalMap threadLocals;  ← package-private   │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ ThreadLocal.ThreadLocalMap                                       │
│   Entry[] table;                  // 초기 INITIAL_CAPACITY = 16  │
│   int size;                                                       │
│   int threshold;                  // 2/3 of capacity → rehash    │
└────────────────────────────┬─────────────────────────────────────┘
                             │
                             ▼ (linear probing)
┌─────────────────────────────────────────────────────────────────┐
│ Entry extends WeakReference<ThreadLocal<?>>                      │
│   ↓ super(k) → ThreadLocal 객체 자체를 WEAK 참조 (key)            │
│   Object value;  ← STRONG 참조 (value)                            │
└─────────────────────────────────────────────────────────────────┘
```

**핵심 비대칭성**: key는 weak reference, value는 strong reference. 이게 모든 ThreadLocal 누수 패턴의 원인.

---

## 2. JDK 21 실제 소스 — Entry 클래스

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);   // ← WeakReference의 referent로 ThreadLocal 등록
        value = v;  // ← 일반 instance field, strong reference
    }
}
```

WeakReference 동작:
- `entry.get()` → ThreadLocal 객체 반환, 단 GC됐으면 null
- `entry.refersTo(key)` → JDK 16+ 추가, identity 비교를 reachability 영향 없이 수행
- `entry.clear()` → referent를 명시적으로 null로 (참조 끊음)

→ ThreadLocal 객체 자체가 **강한 참조 어딘가에 남아있는 한** WeakReference는 살아남는다. 이게 instance field ThreadLocal의 함정.

---

## 2.5. 왜 `Entry[] table` 일반 배열인가? HashMap 안 쓰는 이유

```java
/**
 * The table, resized as necessary.
 * table.length MUST always be a power of two.
 */
private Entry[] table;
```

면접 단골 질문: "왜 `Map<ThreadLocal, Object>` 같은 표준 컬렉션을 쓰지 않고 `Entry[]` 단순 배열을?"

### 2.5.1 Bootstrapping 제약 — 가장 본질적 이유

ThreadLocalMap 위치:

```
java.lang.ThreadLocal$ThreadLocalMap   ← java.lang 패키지 안
```

`java.lang.*` 클래스는 JVM 부팅 시 **가장 먼저** 로드되는 코어 클래스.

만약 `java.util.HashMap` 을 쓰면:
- `java.lang.ThreadLocal` → `java.util.HashMap` 의존
- `java.util.HashMap` 자체도 내부 구현 + 로깅 + 디버깅에서 ThreadLocal 사용 가능성 있음
- → **순환 의존**, 부팅 순서 문제

`Entry[]` 단순 배열은 JVM 부팅 1순위에 사용 가능한 가장 기본 자료구조. 의존성 0.

### 2.5.2 Open Addressing (Linear Probing) — 성능 설계 의도

표준 HashMap 은 **chaining** (각 버킷이 LinkedList).
ThreadLocalMap 은 **open addressing + linear probing** (충돌 시 다음 빈 슬롯).

```
HashMap (chaining):
  table[i] → Node → Node → Node ...    (LinkedList wrapper 객체 추가)

ThreadLocalMap (linear probing):
  table[i]   = Entry
  table[i+1] = Entry  ← 충돌 시 +1 옆 슬롯
  table[i+2] = Entry
```

**Linear probing 선택 이유 (ThreadLocal 사용 패턴 특화)**:

| | HashMap (chaining) | ThreadLocalMap (linear probing) |
|---|---|---|
| 평균 entry 수 | 수천~수백만 | **수십~수백** (스레드당) |
| 충돌 처리 | LinkedList | 다음 슬롯 |
| 메모리 오버헤드 | Node wrapper 객체 추가 | Entry 만으로 충분 |
| Cache locality | LinkedList = 메모리 분산 | **연속 배열 = CPU cache friendly** |
| Stale entry 처리 | 각 LinkedList 순회 | **배열 순차 스캔, expungeStaleEntry 단순** |

→ ThreadLocal 사용 환경 (적은 entry 수, 빈번한 stale 청소) 에서 linear probing 이 압도적 유리.

### 2.5.3 Power of Two 크기 — 빠른 modulo

```java
table.length MUST always be a power of two
```

해시 테이블 인덱스 계산:

```java
// 일반 방식:
int i = hash % table.length;        // 나눗셈 = 느림 (~20 cycles)

// ThreadLocalMap 방식 (length가 2의 거듭제곱):
int i = hash & (table.length - 1);  // 비트 AND = 빠름 (~1 cycle)
```

예시: `table.length = 16` (2^4) → `length - 1 = 15 = 0b1111`
- `hash = 0x6A_5C_3F_82` → `hash & 0xF = 0x2` → `table[2]`
- 나눗셈 한 번이 비트 AND 한 번으로 최적화

### 2.5.4 Multiplicative Hashing (Golden Ratio)

ThreadLocal.threadLocalHashCode 계산:

```java
private static final int HASH_INCREMENT = 0x61c88647;  // golden ratio × 2^32

private static AtomicInteger nextHashCode = new AtomicInteger();

private static int nextHashCode() {
    return nextHashCode.getAndAdd(HASH_INCREMENT);
}
```

**`0x61c88647` = (√5 - 1) / 2 × 2^32 ≈ 황금비**

특징:
- 매 ThreadLocal 객체에 고유 해시 (생성 순서대로 0x61c88647씩 증가)
- 황금비 = 균등 분산 (Knuth Multiplicative Hashing)
- `& (length-1)` 했을 때 충돌 최소화

→ Object.hashCode() 의 identity hash 가 아니라 **ThreadLocal 전용 의도적 spread** 적용.

### 2.5.5 Stale Entry 청소 — 배열이라서 단순한 알고리즘

```java
// expungeStaleEntry 의 핵심:
// 시작 슬롯부터 다음 null entry까지 배열 선형 스캔
//   - entry.get() == null  →  Entry 자체 제거
//   - entry.get() != null  →  rehash (linear probing 깨졌을 수 있으므로)
```

만약 chaining 이었다면:
- 각 LinkedList 의 head/middle/tail 따로 처리 + 노드 삭제 link 재조정 + GC 추적 복잡

배열 + linear probing 이면:
- 단순한 인덱스 순회 + null 체크 + slot 재배치
- `cleanSomeSlots` 의 log2(n) 슬롯 검사도 **연속 메모리 접근으로 cache friendly**

### 2.5.6 정리

| 설계 결정 | 이유 |
|---|---|
| `Entry[]` 단순 배열 | java.lang 패키지의 부팅 의존성 0 |
| Open addressing (linear probing) | 적은 entry + cache locality |
| Power of two 크기 | `& (length-1)` 비트 AND 로 빠른 modulo |
| Golden ratio hash (`0x61c88647`) | 균등 분산 |
| stale entry 배열 스캔 청소 | 알고리즘 단순 + cache friendly |

→ **HashMap 의 범용성 vs ThreadLocalMap 의 특화** 의 트레이드오프. ThreadLocal 의 사용 패턴 (적은 entry, 빈번한 stale 청소, 부팅 단계 의존성 회피) 에 맞춘 의도적 설계.

### 2.5.7 면접 답변

> **"java.lang 패키지 안에 있어서 java.util.HashMap 같은 외부 자료구조 의존을 피해야 하는 부팅 제약이 첫 번째 이유입니다. 거기에 ThreadLocal 의 사용 패턴이 entry 수가 적고(수십~수백) cache locality 가 중요하니까, chaining 보다는 open addressing + linear probing 이 더 효율적이에요. table.length 를 2의 거듭제곱으로 강제해서 비트 AND 로 빠른 modulo 가 가능하고, 황금비 해시(`0x61c88647`)로 균등 분산까지 챙겼습니다. stale entry 청소 (cleanSomeSlots, expungeStaleEntry) 도 배열 선형 스캔이라 알고리즘이 단순하고 cache friendly. 결국 HashMap 의 범용성보다 ThreadLocal 의 특화된 사용 패턴에 맞춘 의도적 설계입니다."**

---

## 2.6. HashMap (Java 8+) vs ThreadLocalMap — 정확한 비교

면접 빈출: "HashMap 이랑 어떻게 달라요? 둘 다 평균 O(1) 아니에요?"

### 2.6.1 HashMap 의 hybrid 구조 (Java 8+)

Java 8 부터 HashMap 의 bucket 은 **LinkedList + Red-Black Tree hybrid**:

```java
static final int TREEIFY_THRESHOLD = 8;     // bucket entry ≥ 8 → tree
static final int UNTREEIFY_THRESHOLD = 6;   // bucket entry ≤ 6 → list
static final int MIN_TREEIFY_CAPACITY = 64; // table ≥ 64 일 때만 tree
```

```
bucket 의 entry 수 < 8 일 때:
  table[i] → Node → Node → Node ...   (LinkedList)

bucket 의 entry 수 ≥ 8 일 때 (table.length ≥ 64 와 결합):
  table[i] → Red-Black Tree (TreeNode)
```

→ Java 7 까진 항상 LinkedList 였고, Java 8 부터 충돌 많아지면 자동으로 tree 로 변환되어 **worst case O(N) → O(log N)** 개선.

### 2.6.2 시간 복잡도 비교

| 작업 | HashMap (Java 8+) | ThreadLocalMap |
|---|---|---|
| 평균 조회 | **O(1)** | **O(1)** |
| 평균 삽입 | **O(1)** | **O(1)** |
| 평균 삭제 | **O(1)** | **O(1)** |
| worst 조회 | O(log N) (tree) | O(N) (linear probing cluster) |
| worst 삽입 | O(log N) | O(N) |

→ **평균은 같음**. 차이는 worst case + 상수항.

### 2.6.3 Big-O 가 안 보여주는 진짜 차이 — 상수항

같은 평균 O(1) 이라도 실제 시간은 **상수항 (Big-O 가 숨기는 부분)** 으로 결정됨.

#### 상수항 차이 1 — 메모리 배치 (cache locality)

```
HashMap (chaining):
  table[0] → Node@0x500 → next:Node@0x900 → next:Node@0xA00
                              ↑                 ↑
                          별개 메모리 주소     매번 jump

  → 노드 따라갈 때마다 cache line miss
  → DRAM 접근 (~100 ns) 비용

ThreadLocalMap (linear probing):
  table = [Entry, Entry, Entry, Entry, ...]   ← 연속 메모리

  → 한 번 cache line 가져오면 옆 슬롯들도 같이 로드됨 (64 bytes / cache line)
  → cache hit (~1 ns)
```

#### 상수항 차이 2 — 객체 wrapper 메모리

```
HashMap.Node:
  ├ object header (16 bytes)
  ├ hash (4 bytes)
  ├ key reference (8 bytes)
  ├ value reference (8 bytes)
  └ next reference (8 bytes)   ← LinkedList 포인터 추가
  ≈ 48 bytes per entry

ThreadLocalMap.Entry:
  ├ WeakReference 부분
  └ value reference (8 bytes)
  ≈ 32 bytes per entry (next 없음)
```

→ HashMap 은 entry 1개당 +16 bytes 추가 + 추가 객체 (GC 비용).

#### 상수항 차이 3 — Worst case 분포

```
HashMap:           tree 변환으로 worst O(log N) 보장
ThreadLocalMap:    linear probing cluster 발생 시 O(N) 가능
                   단 ThreadLocal 환경 N이 작아서 의미 미미
```

### 2.6.4 결론 — 같은 O(1) 이지만 환경 특화

```
HashMap                    = 범용 (entry 수 큰 경우 대비)
ThreadLocalMap             = ThreadLocal 환경 특화
                            (entry 수 적음 + cache locality + 부팅 제약)
```

### 2.6.5 면접 답변

> **"둘 다 평균 O(1) 입니다. 그래서 평균 시간복잡도로는 비교 의미 없고, 차이는 두 곳에 있어요. 첫째는 worst case — HashMap 은 Java 8 이후 red-black tree 변환으로 O(log N), ThreadLocalMap 은 linear probing cluster 시 O(N) 까지 갈 수 있어요. 둘째는 상수항 — ThreadLocalMap 은 wrapper 객체 0 + 연속 배열이라 cache hit, HashMap 은 Node wrapper + 메모리 분산으로 cache miss 가 더 자주 발생합니다. ThreadLocal 의 적은 entry 환경에서는 worst case 도 작고 상수항도 작은 ThreadLocalMap 이 실측 더 빠른 거고요."**

---

## 2.7. 100만 entry 환경에서는? — 확장 한계와 rehash stall

### 2.7.1 두 축의 확장

ThreadLocal 메모리는 곱셈 관계:

```
총 메모리 = 스레드 수 × 스레드당 entry 수 × entry 크기
```

| 스레드 \ entry/thread | 10 | 100 | 1,000 | 100만 |
|---|---|---|---|---|
| 16 | 5 KB | 51 KB | 512 KB | 32 MB ✅ |
| 1,000 | 320 KB | 3 MB | 32 MB | 2 GB ⚠ |
| 100만 | 320 MB | 3.2 GB | 32 GB ❌ | 32 TB ❌❌ |

→ 한 축이 크면 다른 축이 작아야. Virtual Thread 시대(스레드 ↑↑↑)에 ThreadLocal 사용을 줄여야 하는 정량적 이유.

### 2.7.2 100만 entry per thread 시 rehash 비용

```
table 크기 변화:
  16 → 32 → 64 → 128 → ... → 2^21 = 2,097,152 (load factor 2/3 통과)

총 17번 rehash 발생
rehash N번 = 직전 entry 수만큼 복사 (O(N))

누적 작업: geometric series = 약 2 × 2^21 ≈ 400만 작업

각 rehash 비용 (실측):
  length 1,024 → 2,048    : ~수 μs
  length 1,000,000 → 2M   : ~수십 ms
```

### 2.7.3 Rehash Stall — Tail Latency 문제

```
평균 ThreadLocal.get():    10 ns          ← 거의 항상
가끔 rehash 발생:          수십 ms        ← P99 / P99.9 spike
```

**stall 의 정확한 의미** — CPU 가 멈추는 게 아니라 **그 스레드가 O(N) rehash 작업 하느라 다른 ThreadLocal 호출 처리 못 함**.

```
amortized 평균: O(1) ✅
개별 호출 worst: rehash 발생 슬롯에서 spike ❌
```

→ 운영 환경에서 **P99 latency 가 더 중요** 할 때 이게 진짜 비용.

### 2.7.4 면접 답변

> **"100만 entry 환경에서는 평균 O(1) 은 유지되지만 rehash 시점에 P99 latency 가 튑니다. table.length 가 2/3 채워지면 자동 resize 되는데, length 100만 → 200만 rehash 는 entry 100만개 복사 = O(N) 작업 = 수십 ms 걸려요. 그 시간 동안 그 스레드의 다음 ThreadLocal 호출이 막혀서 tail latency 가 spike 합니다. amortized 비용은 O(1) 이지만 P99/P99.9 latency 관점에선 무시 못 할 비용."**

---

## 3. set() 흐름 — 가장 중요한 메서드

`ThreadLocal.set(value)` → 내부 `set(Thread t, T value)` → `map.set(this, value)` → `ThreadLocalMap.set()`:

```java
private void set(ThreadLocal<?> key, Object value) {
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);    // ← 해시 계산

    for (Entry e = tab[i];                          // ← linear probing
         e != null;
         e = tab[i = nextIndex(i, len)]) {

        if (e.refersTo(key)) {
            e.value = value;       // ★ 이미 있으면 value만 교체
            return;
        }

        if (e.refersTo(null)) {    // ★ stale entry 발견
            replaceStaleEntry(key, value, i);
            return;
        }
    }

    tab[i] = new Entry(key, value);                // ★ 새 entry 삽입
    int sz = ++size;
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```

### set() 분기 3가지

1. **같은 key가 이미 있음** (`e.refersTo(key)`) → `e.value = value` 만 변경, Entry 자체는 그대로
2. **stale entry 발견** (`e.refersTo(null)`, key가 GC됨) → `replaceStaleEntry`로 자리 재활용 + 주변 정리
3. **빈 자리** → `new Entry(key, value)` 삽입 + `cleanSomeSlots`로 주변 stale 일부만 정리

→ **분기 1이 RND-771의 함정 지점**. `set(null)`은 정확히 이 분기를 타서 `e.value = null`만 한다. **Entry 자체는 그대로 살아있음**.

---

## 4. remove() — set(null)과의 결정적 차이

```java
private void remove(ThreadLocal<?> key) {
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);

    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {

        if (e.refersTo(key)) {
            e.clear();              // ★ WeakReference referent를 null로 (key 끊음)
            expungeStaleEntry(i);   // ★ Entry 자체를 table에서 제거 + 주변 정리
            return;
        }
    }
}
```

`remove()`는 두 가지를 한다:
1. `e.clear()` — WeakReference의 key 참조 끊기 (이제 entry는 stale 상태)
2. `expungeStaleEntry(i)` — `tab[i] = null` 즉 **테이블에서 Entry 자체를 제거**

대조:
- `set(null)` → `e.value = null` (Entry 살아있음)
- `remove()` → `tab[i] = null` (Entry 죽음)

이 한 줄 차이가 1.15GB 누수의 원인.

---

## 5. expungeStaleEntry — 청소 알고리즘

```java
private int expungeStaleEntry(int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;

    // 1. 시작 슬롯 자체 정리
    tab[staleSlot].value = null;
    tab[staleSlot] = null;
    size--;

    // 2. 다음 null 슬롯까지 rehash
    Entry e;
    int i;
    for (i = nextIndex(staleSlot, len);
         (e = tab[i]) != null;
         i = nextIndex(i, len)) {

        ThreadLocal<?> k = e.get();
        if (k == null) {
            e.value = null;              // ★ stale entry 추가 정리
            tab[i] = null;
            size--;
        } else {
            // 자리 옮기기 (linear probing rehash)
            int h = k.threadLocalHashCode & (len - 1);
            if (h != i) {
                tab[i] = null;
                while (tab[h] != null)
                    h = nextIndex(h, len);
                tab[h] = e;
            }
        }
    }
    return i;
}
```

→ `remove()` 한 번 호출하면 **인접한 stale entry까지 한꺼번에 정리**. 그래서 진짜로 정리하려면 `remove()`를 써야 한다.

---

## 6. cleanSomeSlots — Amortized 정리의 한계

`set()` 마지막에 호출되는 자동 정리 메커니즘:

```java
private boolean cleanSomeSlots(int i, int n) {
    boolean removed = false;
    Entry[] tab = table;
    int len = tab.length;
    do {
        i = nextIndex(i, len);
        Entry e = tab[i];
        if (e != null && e.refersTo(null)) {
            n = len;
            removed = true;
            i = expungeStaleEntry(i);
        }
    } while ( (n >>>= 1) != 0);   // ★ log2(n) 번만 스캔
    return removed;
}
```

**핵심**: stale entry가 있어도 **`log2(n)`만큼만 스캔**. 16 entry 테이블이면 4번. 즉 stale이 멀리 떨어져 있으면 자동 정리가 못 닿는다.

→ 운영에서 9,994 stale entry가 누적된 이유:
- 매 `set()` 마다 4~8 슬롯만 검사
- 새 entry가 새 ThreadLocal 객체에 매칭되면서 hash 분산
- 자동 정리가 따라잡지 못하고 누적

→ 자동 정리는 best-effort, **명시적 `remove()`를 대체하지 못한다**.

---

## 7. WeakReference Key의 정확한 의미

ThreadLocal 누수 학습에서 가장 흔한 오해:

> "ThreadLocal은 WeakReference라서 자동 정리 된다"

**틀렸다.** 정확한 의미는:

- **key (ThreadLocal 객체)** 가 WeakReference로 보호됨 → ThreadLocal 객체에 대한 강한 참조가 사라지면 key 자동 GC 가능
- **value** 는 strong reference → key가 GC돼도 value는 남음 (Entry.value 필드 통해)

→ **언제 정말 자동 정리되는가**:
1. ThreadLocal 객체에 대한 모든 strong 참조가 사라져야 함
2. 그 후 GC가 발생해야 함 (key reference null로 됨, Entry는 stale 상태)
3. 그 다음 `set()` 또는 `remove()` 호출 시 `cleanSomeSlots` / `expungeStaleEntry` 가 우연히 그 슬롯에 닿아야 함
4. 모두 우연이 맞아떨어져야 자동 정리

`BaseClient`의 ThreadLocal은 **instance field**다. 즉 `BaseClient` 인스턴스가 strong reference로 ThreadLocal 객체를 잡고 있다. 인스턴스가 살아있는 한 1번 조건이 안 깨진다.

→ **자동 정리 불가능. 명시적 `remove()` 필수.**

---

## 8. ThreadLocalInspector가 리플렉션을 쓰는 이유

표준 ThreadLocal API는 의도적으로 stale entry를 노출하지 않는다:
- `get()` — value만 반환, Entry 존재 여부 모름
- `isPresent()` — 없음 (JDK가 의도적으로 안 만듦)
- `Thread.threadLocals` — package-private (`java.lang` 외부에서 접근 불가)

→ **stale entry까지 카운트하려면 리플렉션 외 방법이 없다.**

```java
public static int countEntries(Thread thread) {
    Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
    threadLocalsField.setAccessible(true);   // ← 모듈 시스템 우회
    Object threadLocalMap = threadLocalsField.get(thread);
    if (threadLocalMap == null) return 0;

    Field tableField = threadLocalMap.getClass().getDeclaredField("table");
    tableField.setAccessible(true);
    Object[] table = (Object[]) tableField.get(threadLocalMap);

    int count = 0;
    for (Object entry : table) {
        if (entry != null) count++;   // ★ stale 포함 (e.value=null이어도 entry 자체 카운트)
    }
    return count;
}
```

JDK 17+부터 모듈 시스템이 강화되어 `--add-opens=java.base/java.lang=ALL-UNNAMED` JVM 옵션이 필수다. 그래서 production 코드에서 못 쓰고 **테스트 유틸로만 쓴다**.

---

## 9. 요약 표

| 동작 | Entry 변화 | tab[i] | size |
|---|---|---|---|
| `set(value)` (새 key) | new Entry | 새 슬롯 | +1 |
| `set(value)` (기존 key) | `e.value = value` | 그대로 | 0 |
| `set(null)` (기존 key) | `e.value = null` (Entry 살아있음) | **그대로** | 0 |
| `remove()` | `e.clear()` + `tab[i] = null` | **null** | -1 |
| GC만 일어남 | `e.refersTo(null)` (stale) | 그대로 | 0 |
| `cleanSomeSlots` 우연히 맞음 | stale 정리 | null | -1 |

→ **누수 막으려면 `remove()` 만이 결정적 해법**.

---

## 10. 다음 문서로

이 내부 구현 위에 운영 코드의 **per-request `new BaseClient()` 패턴 + `set(null)` 버그**가 결합되면 어떻게 9,994개 stale entry가 누적되는지를 다음 문서(`03-leak-mechanism.md`)에서 추적한다.
