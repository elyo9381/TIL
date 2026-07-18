# Case-004 Simplify V1/V2 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce case-004 to a two-profile repro focused on `v1-legacy` vs `v2-target`.

**Architecture:** Keep the existing repository/scenario runner intact where possible, but collapse the public profile model, tests, API defaults, and docs to two profiles only. Preserve V1 behavior through the current phase3 path and rename it to `v1-legacy`.

**Tech Stack:** Spring Boot, JUnit 5, RedisTemplate, Lua scripts, Gradle

---

### Task 1: Make tests express the two-profile contract

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/test/java/com/yowon/psc/case004/integration/Case004ConsistencyIntegrationTest.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/test/java/com/yowon/psc/case004/integration/Case004ConsistencyApiIntegrationTest.java`

**Step 1: Write the failing test**
- Replace `v1-p3`/`v1-p4` expectations with `v1-legacy` only and keep `v2-target`.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks`
- Expected: FAIL because enum/report/API still expose old profile names.

**Step 3: Write minimal implementation**
- Update enum/report/service/controller to satisfy the two-profile contract.

**Step 4: Run test to verify it passes**
- Run the same module test command and expect PASS.

### Task 2: Collapse profile model and service output

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/domain/Case004Profile.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/domain/Case004CompareReport.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/application/Case004ReproService.java`

**Step 1: Remove `v1-p4` from the public profile enum**
**Step 2: Rename `v1-p3` semantics to `v1-legacy`**
**Step 3: Update compare/report output to expose only `v1Legacy` and `v2Target`**
**Step 4: Run the module tests**

### Task 3: Keep repository behavior but stop exposing the extra V1 variant

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/infra/Case004RedisRepository.java`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/seed/Case004RedisSeedInitializer.java`

**Step 1: Keep the current legacy path as the single V1 implementation**
**Step 2: Remove comments/defaults that imply three public profiles**
**Step 3: Run the module tests**

### Task 4: Simplify API and docs

**Files:**
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/README.md`
- Modify: `problem-solving-cases/docs/blog-04-proxy-v1-v2-consistency.md`
- Modify: `problem-solving-cases/docs/proxy-v1-v2-series-index.md`
- Modify: `problem-solving-cases/case-004-proxy-v1-v2-consistency/src/main/java/com/yowon/psc/case004/repro/api/Case004ReproController.java`

**Step 1: Update API examples/defaults to new profile names**
**Step 2: Remove `v1-p4` references from README/blog docs**
**Step 3: Reword explanation to `V1 legacy vs V2 target`**
**Step 4: Run tests and review docs for stale names**

### Task 5: Verify and summarize

**Step 1: Run `./gradlew :case-004-proxy-v1-v2-consistency:test --rerun-tasks` in `problem-solving-cases/`**
**Step 2: Run grep for removed profile names**
**Step 3: Summarize remaining structural cleanup opportunities**
