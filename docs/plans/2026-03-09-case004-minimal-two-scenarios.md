# Case004 Minimal Two Scenarios Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce `case-004` to a minimal `v1-legacy` vs `v2-target` repro focused only on duplicate borrow and retry/orphan consistency.

**Architecture:** Keep the existing split repositories, but remove comparison/reporting surfaces and non-essential parameters so the module exposes one profile report API and only the three consistency metrics. Preserve Redis-backed behavior for the two scenarios that support the blog narrative: concurrent borrow races and retry after partial failure.

**Tech Stack:** Spring Boot 2.7, RedisTemplate/StringRedisTemplate, JUnit 5, MySQL-free Redis integration tests

---

### Task 1: Trim the API and command model to the minimal inputs

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/api/Case004ReproController.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/domain/Case004CompareCommand.java`
- Delete: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/domain/Case004CompareReport.java`

**Step 1: Write/adjust failing API test expectations**
- Update API test to assert `/compare` is gone and `/report` only needs `profile`, `domain`, `concurrency`, `poolSize`.

**Step 2: Run the targeted API test to verify failure**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --tests "*Case004ConsistencyApiIntegrationTest" --rerun-tasks`

**Step 3: Implement minimal API surface**
- Remove compare endpoint
- Replace command fields with only `domain`, `concurrency`, `poolSize`

**Step 4: Re-run targeted API test**
- Expect PASS

### Task 2: Remove non-essential report fields and keep only consistency metrics

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/domain/Case004ProfileReport.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/application/Case004ReproService.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/test/java/com/yowon/psc/case004/integration/Case004ConsistencyIntegrationTest.java`

**Step 1: Update integration test expectations to only assert**
- `duplicateLeaseCount`
- `orphanLeaseCount`
- `idempotencyMismatchCount`

**Step 2: Run targeted integration test to verify failure**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --tests "*Case004ConsistencyIntegrationTest" --rerun-tasks`

**Step 3: Implement minimal report model**
- Remove latency/success/fail aggregates from profile report and service assembly

**Step 4: Re-run targeted integration test**
- Expect PASS

### Task 3: Collapse the service to exactly two scenarios

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/application/Case004ReproService.java`

**Step 1: Refactor tests to match two-scenario behavior**
- Ensure tests describe:
  - concurrent borrow duplicate behavior
  - retry/orphan/idempotency behavior

**Step 2: Run tests to verify failure if any stale behavior remains**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --tests "*Case004ConsistencyIntegrationTest" --rerun-tasks`

**Step 3: Simplify service**
- Remove iteration loops
- Remove latency collection
- Remove cooldown/appCount handling
- Run exactly one concurrent borrow scenario and one retry/orphan scenario per report

**Step 4: Re-run tests**
- Expect PASS

### Task 4: Update docs and seed defaults to the minimal story

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/README.md`
- Modify: `problem-solving-cases/README.md`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/seed/Case004RedisSeedInitializer.java`

**Step 1: Update README examples and defaults**
- Document only `/report`
- Document only two scenarios and three metrics

**Step 2: Verify docs and boot defaults are coherent**
- Use `rg` to ensure removed params/endpoints are not advertised

### Task 5: Full verification

**Files:**
- Verify modified files above

**Step 1: Run full case-004 test suite**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks`

**Step 2: Inspect git diff for accidental complexity**
- Run: `git diff -- problem-solving-cases/case-004-proxy-v1-v2-consistency problem-solving-cases/README.md`

**Step 3: Summarize remaining cleanup candidates without implementing them**
- Keep note of optional future split if still needed
