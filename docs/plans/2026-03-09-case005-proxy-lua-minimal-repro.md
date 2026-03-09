# Case005 Proxy Lua Minimal Repro Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a small Redis-backed repro module that demonstrates why Lua + requestId were introduced for proxy borrow consistency.

**Architecture:** Add a new Spring Boot submodule with two tiny service implementations: V1 legacy borrow based on first-fit read-modify-write, and V2 target borrow/return based on Lua scripts loaded from resources. Back the behavior with Redis Testcontainers integration tests that cover only concurrent borrow and retry after partial failure.

**Tech Stack:** Java 11, Spring Boot 2.7, Spring Data Redis, JUnit 5, Testcontainers Redis

---

### Task 1: Register the new module and create the build skeleton

**Files:**
- Modify: `problem-solving-cases/settings.gradle.kts`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/build.gradle.kts`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/Case005ProxyLuaMinimalReproApplication.java`

**Step 1: Write the failing structural test**
- Add a simple Spring context test file that references the new application class.

**Step 2: Run the targeted test to verify module/config failure**
- Run: `./gradlew :case-005-proxy-lua-minimal-repro:test --tests "*Case005ContextIntegrationTest" --rerun-tasks`

**Step 3: Add the module include and build file**
- Reuse the Redis/Testcontainers dependency shape from case-004, but omit web starter.

**Step 4: Re-run the targeted test**
- Expect compilation/context progress toward green.

### Task 2: Add the Redis support and V1 legacy service

**Files:**
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/repro/infra/ProxyPoolSeedSupport.java`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/repro/v1/LegacyBorrowService.java`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/test/java/com/yowon/psc/case005/support/RedisIntegrationTestSupport.java`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/test/java/com/yowon/psc/case005/integration/Case005ConcurrentBorrowIntegrationTest.java`

**Step 1: Write the failing concurrent V1 test**
- Assert duplicate assignment is observable under forced race delay.

**Step 2: Run the targeted test to verify failure**
- Run: `./gradlew :case-005-proxy-lua-minimal-repro:test --tests "*Case005ConcurrentBorrowIntegrationTest" --rerun-tasks`

**Step 3: Implement seed support and minimal V1 borrow path**
- Use `v1:order` list and `v1:resource:{id}` hash with owner JSON.

**Step 4: Re-run the targeted test**
- Expect V1 duplicate scenario to pass.

### Task 3: Add Lua-based V2 service and resources

**Files:**
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/repro/v2/LuaBorrowService.java`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/resources/lua/borrow.lua`
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/resources/lua/return.lua`
- Modify: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/test/java/com/yowon/psc/case005/integration/Case005ConcurrentBorrowIntegrationTest.java`

**Step 1: Extend the concurrent test with V2 expectations**
- Assert V2 duplicate assignment count is zero.

**Step 2: Run the targeted test to verify failure**
- Run: `./gradlew :case-005-proxy-lua-minimal-repro:test --tests "*Case005ConcurrentBorrowIntegrationTest" --rerun-tasks`

**Step 3: Implement Lua-backed borrow service**
- Load scripts from resource files, not Java string constants.

**Step 4: Re-run the targeted test**
- Expect both V1 and V2 assertions to pass.

### Task 4: Add retry/partial-failure regression test

**Files:**
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/test/java/com/yowon/psc/case005/integration/Case005RetryConsistencyIntegrationTest.java`
- Modify: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/repro/v1/LegacyBorrowService.java`
- Modify: `problem-solving-cases/case-005-proxy-lua-minimal-repro/src/main/java/com/yowon/psc/case005/repro/v2/LuaBorrowService.java`

**Step 1: Write the failing retry consistency test**
- V1 should expose orphan or mismatch.
- V2 should return consistent result on retry.

**Step 2: Run the targeted test to verify failure**
- Run: `./gradlew :case-005-proxy-lua-minimal-repro:test --tests "*Case005RetryConsistencyIntegrationTest" --rerun-tasks`

**Step 3: Implement the minimal behavior needed**
- V1 uses write-then-throw simulation.
- V2 uses requestId result cache + Lua.

**Step 4: Re-run the targeted test**
- Expect green.

### Task 5: Add module README and full verification

**Files:**
- Create: `problem-solving-cases/case-005-proxy-lua-minimal-repro/README.md`
- Modify: `problem-solving-cases/README.md`

**Step 1: Document the module scope**
- State clearly that this is a minimal borrow consistency repro, not the full proxy architecture.

**Step 2: Run full module tests**
- Run: `./gradlew :case-005-proxy-lua-minimal-repro:test --rerun-tasks`

**Step 3: Inspect diff for accidental complexity**
- Run: `git diff -- problem-solving-cases/case-005-proxy-lua-minimal-repro problem-solving-cases/README.md`
