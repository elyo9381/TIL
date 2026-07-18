# Problem Solving Cases Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `problem-solving-cases` 멀티모듈 프로젝트를 생성하고 `case-001-n1-cte`를 실행 가능한 형태로 완성한다.

**Architecture:** 루트는 Gradle Kotlin DSL 멀티모듈로 구성하고, `:common`에 공통 측정/설정 로직을 둔다. `:case-001-n1-cte`는 baseline(JPA 연관 접근)과 optimized(CTE native query) 두 경로를 제공해 결과와 성능을 비교한다.

**Tech Stack:** Java 11, Spring Boot 2.7.x, Spring Data JPA, MySQL 8, Gradle Kotlin DSL, JUnit 5

---

### Task 1: 프로젝트 골격 생성

**Files:**
- Create: `problem-solving-cases/settings.gradle.kts`
- Create: `problem-solving-cases/build.gradle.kts`
- Create: `problem-solving-cases/gradle.properties`
- Create: `problem-solving-cases/.env.example`
- Create: `problem-solving-cases/docker-compose.yml`
- Create: `problem-solving-cases/README.md`

**Step 1: Write the failing test**
- 없음(인프라/구성 작업)

**Step 2: Run test to verify it fails**
- 스킵(구성만 생성)

**Step 3: Write minimal implementation**
- 멀티모듈 설정 및 실행 문서 생성

**Step 4: Run test to verify it passes**
Run: `./gradlew projects`
Expected: `:common`, `:case-001-n1-cte` 노출

**Step 5: Commit**
```bash
git add problem-solving-cases
git commit -m "chore: bootstrap problem-solving-cases multi-module"
```

### Task 2: common 모듈 구현

**Files:**
- Create: `problem-solving-cases/common/build.gradle.kts`
- Create: `problem-solving-cases/common/src/main/java/.../QueryMetricsRecorder.java`
- Create: `problem-solving-cases/common/src/main/java/.../QueryMetricSnapshot.java`
- Test: `problem-solving-cases/common/src/test/java/.../QueryMetricsRecorderTest.java`

**Step 1: Write the failing test**
- 스냅샷 생성과 reset 동작 테스트 작성

**Step 2: Run test to verify it fails**
Run: `./gradlew :common:test --tests "*QueryMetricsRecorderTest"`
Expected: 컴파일/테스트 실패

**Step 3: Write minimal implementation**
- Recorder 구현 및 스냅샷 모델 추가

**Step 4: Run test to verify it passes**
Run: `./gradlew :common:test --tests "*QueryMetricsRecorderTest"`
Expected: PASS

**Step 5: Commit**
```bash
git add problem-solving-cases/common
git commit -m "feat(common): add query metrics recorder"
```

### Task 3: case-001 baseline/optimized API 구현

**Files:**
- Create: `problem-solving-cases/case-001-n1-cte/build.gradle.kts`
- Create: `problem-solving-cases/case-001-n1-cte/src/main/java/...`
- Test: `problem-solving-cases/case-001-n1-cte/src/test/java/...`

**Step 1: Write the failing test**
- baseline/optimized 서비스 매핑 테스트 작성

**Step 2: Run test to verify it fails**
Run: `./gradlew :case-001-n1-cte:test --tests "*ReservationService*"`
Expected: 컴파일/테스트 실패

**Step 3: Write minimal implementation**
- 엔티티, 리포지토리, 서비스, 컨트롤러 구현

**Step 4: Run test to verify it passes**
Run: `./gradlew :case-001-n1-cte:test --tests "*ReservationService*"`
Expected: PASS

**Step 5: Commit**
```bash
git add problem-solving-cases/case-001-n1-cte
git commit -m "feat(case-001): implement baseline and cte optimized api"
```

### Task 4: 데이터 시드 및 케이스 문서화

**Files:**
- Create: `problem-solving-cases/case-001-n1-cte/src/main/resources/application.yml`
- Create: `problem-solving-cases/case-001-n1-cte/src/main/java/.../SeedDataInitializer.java`
- Create: `problem-solving-cases/case-001-n1-cte/docs/problem.md`
- Create: `problem-solving-cases/case-001-n1-cte/docs/hypothesis.md`
- Create: `problem-solving-cases/case-001-n1-cte/docs/validation.md`

**Step 1: Write the failing test**
- 시드 후 데이터 개수 조건 검증 테스트 작성

**Step 2: Run test to verify it fails**
Run: `./gradlew :case-001-n1-cte:test --tests "*SeedDataInitializerTest"`
Expected: FAIL

**Step 3: Write minimal implementation**
- 시드 구현과 문서 템플릿 추가

**Step 4: Run test to verify it passes**
Run: `./gradlew :case-001-n1-cte:test --tests "*SeedDataInitializerTest"`
Expected: PASS

**Step 5: Commit**
```bash
git add problem-solving-cases/case-001-n1-cte
git commit -m "docs(case-001): add reproducible case docs and seed initializer"
```

### Task 5: 통합 검증

**Files:**
- Modify: `problem-solving-cases/README.md`

**Step 1: Write the failing test**
- 없음(검증 단계)

**Step 2: Run test to verify it fails**
- 스킵

**Step 3: Write minimal implementation**
- 실행/검증 절차 보강

**Step 4: Run test to verify it passes**
Run: `./gradlew clean test`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**
```bash
git add problem-solving-cases/README.md
git commit -m "docs: add runbook for case-001 verification"
```
