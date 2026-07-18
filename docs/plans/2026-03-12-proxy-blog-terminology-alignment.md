# Proxy Blog Terminology Alignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align reader-facing terminology across the four proxy blog docs so the language is easier to scan and less tied to code variable names.

**Architecture:** Keep the existing document structure and only adjust wording. Replace code-smelling or overly technical labels with a stable public vocabulary shared across the two blog posts, the architecture note, and the series index.

**Tech Stack:** Markdown docs, ripgrep, shell text replacement

---

### Task 1: Define the public terminology set

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/proxy-borrow-return-architecture.md`
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/proxy-v1-v2-series-index.md`

**Step 1: Replace selection-policy wording**
- First mention: `순차적 선택(first-fit)`
- Later mentions: `순차적 선택`

**Step 2: Replace reuse-delay wording**
- Code-like predicate in pseudocode: `reuseDelayPassed(...)`
- Prose: `반납 후 재사용 대기 시간이 지났는지`

**Step 3: Keep V2 pool wording stable**
- `도메인별 기준 풀`
- `할당용 순환 큐`
- `락 기반 보수 경로`

### Task 2: Verify wording consistency

**Files:**
- Verify: the same four docs above

**Step 1: Search for old wording**
Run: `rg -n "cooldownPassed|first-fit|First-fit" <four-doc-paths>`
Expected: only the intentional first-use `순차적 선택(first-fit)` remains, and `cooldownPassed` no longer appears.

**Step 2: Spot-check each doc**
Run: `sed -n '1,180p' <doc>` as needed
Expected: headings and body text read naturally without code-smelling variable names.
