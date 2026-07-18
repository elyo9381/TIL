# 07 — Virtual Thread + ScopedValue + 2026 도입 현황

> ThreadLocal 의 시대적 후계자 + 2026년 5월 기준 production 도입 현황 정리.
> 면접 빈출: "Virtual Thread 도입 검토 해보셨어요?", "ScopedValue 알아요?"

---

## 0. 학습 목적

- Platform Thread 와 Virtual Thread 의 메모리/구조 차이
- ThreadLocal vs ScopedValue 의 4가지 핵심 차이
- 2026년 시점 라이브러리 마이그레이션 + 운영 도입 현황
- 회사 환경별 도입 판단 기준

---

## 1. Java 버전 타임라인 (2026년 5월 기준)

| 버전 | 출시 | 핵심 |
|---|---|---|
| Java 19 | 2022.9 | Virtual Thread (preview) |
| **Java 21 LTS** | **2023.9** | **Virtual Thread 정식** (JEP 444) |
| Java 22 | 2024.3 | Pinning 일부 해결 (ReentrantLock, etc) |
| Java 23 | 2024.9 | ScopedValue (preview) |
| Java 24 | 2025.3 | ScopedValue (2nd preview) |
| **Java 25 LTS** | **2025.9** | **ScopedValue 정식** (JEP 506) |
| **Java 26** | **2026.3** | Virtual Thread + ScopedValue 안정화 완성 |

→ **2026년 5월 현재**: Virtual Thread 와 ScopedValue 모두 production 안정화 완료.

---

## 2. Platform Thread vs Virtual Thread

### 2.1 Platform Thread (전통적 java Thread)

```
1 스레드 = OS 스레드 1개 (1:1 매핑)

각 스레드 메모리:
  ├─ Stack:        1 MB (기본값, -Xss 옵션으로 변경)
  ├─ Thread 객체:   몇 KB
  ├─ ThreadLocalMap: 16 슬롯 × 8 bytes = 128 bytes (초기)
  └─ TLAB 등:       수 KB
  ─────────────────────────────────
  합:            약 1 MB / 스레드

100만 스레드 = 1 TB 메모리 ❌ 불가능
```

### 2.2 Virtual Thread (Java 21+)

```
1 vthread = JVM 안의 가벼운 작업 단위 (M:N 매핑)
  - M개 vthread 가 N개 carrier thread (실제 OS 스레드) 위에서 돌아감
  - N = CPU 코어 수 (보통 8개)

각 vthread 메모리:
  ├─ Stack:        ~ 수 KB (필요한 만큼 동적 확장)
  ├─ Thread 객체:   몇 KB
  └─ ThreadLocalMap: 있으면 추가
  ─────────────────────────────────
  합:            약 수 KB / vthread

100만 vthread = 수 GB ✅ 가능
```

### 2.3 어떻게 가능한가? — Continuation 기반

```
[Platform Thread]
요청 처리:
  thread1 → DB 호출 (대기 100ms) → thread1 그동안 idle
                                            ↑
                                            CPU 자원 낭비

[Virtual Thread]
요청 처리:
  vthread1 → DB 호출 (대기)
            → carrier thread 에서 unmount
            → carrier 는 vthread2 처리 (재사용)
            → DB 응답 오면 vthread1 다시 어떤 carrier 위에서 mount
                                            ↑
                                            CPU 자원 효율
```

핵심: **I/O 대기 중 vthread 가 carrier 에서 떨어져 다른 vthread 가 carrier 사용**.

### 2.4 Pinning — Virtual Thread 의 함정

vthread 가 carrier thread 에서 unmount 못 하는 상태:

```java
// 1. synchronized + I/O
synchronized (lock) {
    db.query();   // ← I/O 발생, 그러나 unmount 불가능
}

// 2. ThreadLocal 의존 라이브러리 + synchronized
synchronized (lock) {
    String userId = SecurityContextHolder.getContext()
                                          .getAuthentication()
                                          .getName();   // ThreadLocal 의존
    httpClient.call();   // ← I/O, pinning 발생
}

// 3. native method 안의 I/O
synchronized native void someMethod();
```

→ Virtual Thread 의 핵심 가치 (M:N 매핑) 가 깨짐.

**Java 22+ 부터 Pinning 일부 자동 해결**:
- `ReentrantLock` 안의 I/O → 자동 unmount
- `synchronized` 안의 I/O → 여전히 pinning (구조적 한계)

**Pinning 검출**:
```bash
-Djdk.tracePinnedThreads=full   # 어떤 코드가 pinning 일으키는지 로그
```

---

## 3. ThreadLocal vs ScopedValue — 4가지 핵심 차이

### 3.1 사용 비교

```java
// ThreadLocal (전통적)
private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

void handleRequest(User user) {
    CURRENT_USER.set(user);
    try {
        doWork();
    } finally {
        CURRENT_USER.remove();   // ❌ 빼먹으면 누수 (RND-771 본질)
    }
}

// ScopedValue (Java 21+, 정식 Java 25)
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

void handleRequest(User user) {
    ScopedValue.where(CURRENT_USER, user)
        .run(() -> {
            doWork();
            // 이 람다 안에서만 CURRENT_USER.get() 가능
        });
    // 람다 끝나면 자동 cleanup, 누수 불가능
}

void doWork() {
    User u = CURRENT_USER.get();   // 람다 안에서는 OK
}
```

### 3.2 4가지 핵심 차이

| | ThreadLocal | ScopedValue |
|---|---|---|
| **저장소** | 각 Thread 의 ThreadLocalMap (heap) | call stack 의 immutable frame |
| **수명** | set ~ remove (사용자 책임) | scope (람다) 동안만 |
| **변경** | 어디서든 set 가능 (mutable) | scope 안에서 immutable |
| **누수** | remove 누락 시 발생 | **불가능** (자동 cleanup) |

### 3.3 메모리 효율 (Virtual Thread 환경)

```
ThreadLocal:
  스레드 N개 × 각자의 ThreadLocalMap = N × ThreadLocalMap 메모리
  vthread 100만 × ThreadLocal 1개 = 32 MB (multiplier 폭발)

ScopedValue:
  현재 실행 중인 메서드 stack frame 에만 저장
  vthread unmount 시 stack 도 같이 빠짐
  → 진짜 활성 vthread 만 메모리 점유
```

### 3.4 자동 inheritance (자식 vthread 에 자동 전파)

```java
ScopedValue.where(CURRENT_USER, user)
    .run(() -> {
        doWork();   // CURRENT_USER 보임

        Thread.startVirtualThread(() -> {
            User u = CURRENT_USER.get();   // ← 자식 vthread 에도 자동 전파
        });
    });
```

ThreadLocal 은 `InheritableThreadLocal` 별도 사용 + 명시적 처리 필요. ScopedValue 는 자동.

### 3.5 RND-771 관점에서

```
RND-771 의 본질 = "ThreadLocal cleanup 인터페이스 누락"

ScopedValue 시대:
  cleanup 자체가 구조적으로 불가능 (scope 끝나면 자동)
  → RND-771 같은 실수가 일어날 수 없는 설계
```

→ ScopedValue 가 ThreadLocal 의 후계자인 가장 강한 이유.

---

## 4. 2026년 5월 production 도입 현황

### 4.1 글로벌 도입 사례

#### Netflix
```
2024.8: Virtual Thread 도입 후 pitfall 발견
        - synchronized + ReentrantLock + I/O 결합 pinning
        - 일부 인스턴스 traffic 처리 불가
        (블로그: "Java 21 Virtual Threads — Dude, Where's My Lock?")

2024 말: Java 22+ JDK 패치로 ReentrantLock pinning 해결

2025-2026: 마이크로서비스 fleet 전반 안정 운영
```

#### LinkedIn, Spotify, JetBrains
- 일부 / 다수 서비스 production 운영 중
- 신규 마이크로서비스 우선 적용

### 4.2 한국 시장 (2026.5)

| 회사 | 도입 상태 |
|---|---|
| 네이버 | 일부 신규 서비스 (검색 / 쇼핑 일부) |
| 카카오 | 검토 + 일부 도입 (모빌리티 / 페이 신규 모듈) |
| 토스 | 신규 프로젝트 도입 증가 |
| 일반 SI / 레거시 | Java 17 / Spring Boot 2.7 다수, 미도입 |

→ **신규 프로젝트는 도입 활발, 레거시는 Platform Thread 유지**.

### 4.3 라이브러리 마이그레이션 현황

#### ✅ 진행 완료 (2026.5)
```
Spring Security      - SecurityContextHolder 의 ScopedValue 전략 정식
Spring Boot 3.4      - Tomcat / Jetty Virtual Thread executor 정식
Reactor              - 원래부터 ThreadLocal 비의존
Logback              - MDC가 Virtual Thread 자동 propagation 지원
HikariCP             - synchronized 블록 제거 (pinning 해결)
```

#### 🟡 진행 중
```
Spring MVC           - RequestContextHolder ScopedValue 마이그레이션 검토
Hibernate            - PersistenceContext 의 ThreadLocal 의존 (구조적 한계)
Spring Cloud         - Sleuth / Trace context 마이그레이션 진행
```

#### ⚠ 알려진 제약
```
JPA  - PersistenceContext 가 ThreadLocal 의존 (마이그레이션 어려움)
JNI / native call - 여전히 pinning 발생
일부 JDBC 드라이버 - synchronized 사용 (해결 진행 중)
```

---

## 5. 회사 환경별 도입 판단

### 5.1 신규 마이크로서비스
```
✅ Virtual Thread + ScopedValue 채택 권장

이유:
  - 외부 API 다수 호출 시 메리트 큼 (I/O 대기 많음)
  - WebFlux 대비 단순한 동기 코드
  - ScopedValue 가 ThreadLocal 누수 위험 제거
```

### 5.2 레거시 Spring Boot 2.7 / Java 17
```
⚠ 마이그레이션 비용 vs 메리트 계산

이유:
  - 운영 안정 시 무리한 도입 비추천
  - Java 21 + Spring Boot 3.4 마이그레이션이 우선
  - Virtual Thread 는 그 다음 단계
```

### 5.3 고동시성 환경 (게임 / 메시징 / 실시간)
```
✅ 도입 강력 권장

이유:
  - 동시 연결 수만 ~ 수십만
  - Platform Thread 로는 메모리 한계
  - WebSocket / SSE 같은 영구 연결에 적합
```

### 5.4 일반 CRUD 웹서비스
```
도입해도 됨 / 안 해도 됨

이유:
  - 동시 사용자 수천 ~ 수만 (Platform Thread 200개로 충분)
  - I/O latency 짧음 (대기 시간 적음)
  - 메리트 작음, 안 해도 문제 없음
```

---

## 6. 회사 입장 적용 (case-006 / RND-771 관점)

```
환경: ptspro-iros-inq, RabbitMQ 컨슈머 16개, Java 21 + Spring Boot 3.x 가정

도입 메리트: 작음 (스레드 16개)

권장:
1. 레거시 (이미 운영 중인 ptspro-iros-inq)
   → RND-771 fix 그대로 유지 (close()에 super.clearRequestState)
   → Virtual Thread 도입 안 해도 OK

2. BaseClient 라이브러리 점진 마이그레이션 검토
   → ThreadLocal cleanup 인터페이스 강제 → ScopedValue 전환 (장기 목표)
   → 누수 자체가 구조적으로 불가능

3. 신규 마이크로서비스 / 외부 API 다수 호출 환경
   → Virtual Thread + ScopedValue 우선 채택

4. ThreadLocal 누수의 amplification 위험
   → Virtual Thread 환경에서 RND-771 같은 누수 = 메모리 폭발
   → ScopedValue 채택의 실용적 근거
```

---

## 7. 면접 답변 정리

### Q1. "Virtual Thread 가 뭐예요?"
> "Java 21 부터 정식 도입된 가벼운 스레드 단위입니다. Platform Thread 가 1MB stack 으로 100만 개면 1TB 메모리인 반면, Virtual Thread 는 수 KB 라서 100만 개도 수 GB 면 됩니다. M:N 매핑 — M개 vthread 가 N개 carrier thread (CPU 코어 수) 위에서 돌아가고, I/O 대기 중에 carrier 에서 unmount 돼서 다른 vthread 가 carrier 를 쓸 수 있는 Continuation 기반 구조죠."

### Q2. "ScopedValue 가 뭐예요? ThreadLocal 과 어떻게 달라요?"
> "Java 25 LTS 부터 정식인 ThreadLocal 의 후계자입니다. 4가지 핵심 차이가 있어요. 첫째 저장소가 stack frame 이라 ThreadLocalMap 안 씀, 둘째 람다 scope 동안만 유효해서 cleanup 자동, 셋째 immutable, 넷째 자식 vthread 에 자동 inheritance. RND-771 같은 cleanup 누락 누수가 구조적으로 불가능합니다."

### Q3. "Virtual Thread 도입 경험 있어요?"
> "2026년 현재 production 도입 가능한 안정 상태예요. Netflix 가 2024년에 pinning 같은 pitfall 을 겪었지만 Java 22+ 패치로 해결됐고, Spring Boot 3.4 의 Virtual Thread 옵션이 정식이라 신규 마이크로서비스에는 채택해볼 만합니다. 다만 ptspro-iros-inq 같은 RabbitMQ 컨슈머 16개 환경은 메리트가 작아서 도입 안 했고, 대신 ScopedValue 같은 새 API 는 신규 코드부터 적용 검토 중이에요."

### Q4. "Virtual Thread 가 ThreadLocal 누수에 어떤 영향?"
> "vthread 가 100만 개 환경이면 ThreadLocal 1개당 32MB 메모리 multiplier 가 됩니다. RND-771 같은 누수가 발생하면 16 컨슈머 × 625 entry = 9,997 entry 였던 게, vthread 100만 환경에선 100만 × 625 = 6억 entry 로 즉시 시스템 다운. 즉 Virtual Thread 시대에는 ThreadLocal 누수가 amplifier 가 더 커서 ScopedValue 마이그레이션이 사실상 강제 됩니다."

### Q5. "라이브러리들의 ThreadLocal 은 어떻게?"
> "Spring, Hibernate, Logback 같은 라이브러리들이 내부적으로 ThreadLocal 30~50개씩 써요. 2026년 시점 Spring Boot 3.4 까지 Virtual Thread 옵션은 정식인데, 내부 ThreadLocal 마이그레이션은 호환성 때문에 점진적이에요. Spring Security 의 SecurityContextHolder 는 ScopedValue 전략이 정식 지원, MVC 의 RequestContextHolder 는 검토 중. Hibernate 의 PersistenceContext 는 구조적 한계로 마이그레이션 어려움. 그래서 도입 시 -Djdk.tracePinnedThreads=full 로 실제 pinning 모니터링이 필수입니다."

---

## 8. 핵심 한 줄

> **"Virtual Thread 시대에는 스레드 수가 폭증하니 ThreadLocal 의 메모리 multiplier 가 폭발한다. ScopedValue 가 그 시대의 thread-local 데이터 표준이고, RND-771 같은 cleanup 누락 누수가 구조적으로 불가능한 설계라는 점이 가장 큰 가치."**

---

## 9. 다음 학습

- 06번 — Reference 4종 + ThreadLocalMap 비대칭 설계
- 02번 2.5~2.7장 — table 자료구조 + HashMap 비교 + rehash stall
- 05번 — 6가지 결정의 "왜" + 면접 Q&A 통합
