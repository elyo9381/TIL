# Case-004 Repository Split Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Split the case-004 Redis repository into legacy, target, and shared scenario support components without changing public behavior.

**Architecture:** Move V1-specific logic to a legacy repository, V2 Lua logic to a target repository, and reset/seed/run-lock/shared Redis helpers to scenario support. Update the repro service to depend on the three narrower collaborators instead of the current god repository.

**Tech Stack:** Spring Boot, JUnit 5, RedisTemplate, Lua scripts, Gradle

---

### Task 1: Write the failing split-contract test

**Files:**
- Create: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/test/java/com/yowon/psc/case004/integration/Case004RepositorySplitIntegrationTest.java`

**Step 1: Write the failing test**
- Assert Spring context exposes `Case004LegacyRedisRepository`, `Case004TargetRedisRepository`, and `Case004ScenarioRedisSupport`.
- Assert `Case004RedisRepository` no longer exists.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --tests "*Case004RepositorySplitIntegrationTest" --rerun-tasks`
- Expected: FAIL because old repository still exists.

### Task 2: Introduce shared scenario support

**Files:**
- Create: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/infra/Case004ScenarioRedisSupport.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/seed/Case004RedisSeedInitializer.java`

**Step 1: Move prefix, clearAll, seed, resetScenario, run-lock methods into support**
**Step 2: Keep key helpers needed by both paths accessible from support**
**Step 3: Run the new split-contract test**

### Task 3: Introduce legacy and target repositories

**Files:**
- Create: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/infra/Case004LegacyRedisRepository.java`
- Create: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/infra/Case004TargetRedisRepository.java`
- Delete: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/infra/Case004RedisRepository.java`

**Step 1: Move V1 legacy borrow/count logic into legacy repository**
**Step 2: Move V2 borrow/return/count logic and Lua scripts into target repository**
**Step 3: Keep only path-specific key helpers inside each repository**
**Step 4: Run the split-contract test**

### Task 4: Update service wiring

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/application/Case004ReproService.java`

**Step 1: Inject support + legacy + target repositories**
**Step 2: Route legacy calls to legacy repository, target calls to target repository, reset/lock to support**
**Step 3: Run the full case-004 test suite**

### Task 5: Verify and summarize

**Step 1: Run `./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks`**
**Step 2: Run grep to confirm `Case004RedisRepository` is gone**
**Step 3: Summarize the new structure and remaining simplification opportunities**
