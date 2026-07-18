# Proxy Blog Series Docs Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** V1/V2 프록시 구조와 Redis 정합성/선택 정책을 설명하는 블로그용 문서를 추가한다.

**Architecture:** `docs/proxy-borrow-return-architecture.md`를 원문 기준 문서로 두고, 이를 바탕으로 1편 정합성 글과 2편 선택 정책 글을 분리한다. 1편은 `case-005-proxy-lua-minimal-repro`를 근거로 사용하고, 2편은 실제 운영 구조의 `pool:{domain}`/`cache:proxies:{domain}`/single-writer 전환 논점을 다룬다.

**Tech Stack:** Markdown, Mermaid, existing refine docs, case-005 repro module

---

### Task 1: Add architecture source document

**Files:**
- Create: `docs/proxy-borrow-return-architecture.md`

**Step 1:** Preserve the approved Mermaid diagrams and architecture narrative.
**Step 2:** Clarify V1, V2, transitional, and target single-writer states.
**Step 3:** Link to relevant source code paths.

### Task 2: Add blog draft for Part 1 (consistency)

**Files:**
- Create: `docs/blog-01-why-proxy-v2-was-needed.md`
- Reference: `problem-solving-cases/case-005-proxy-lua-minimal-repro/**`

**Step 1:** Explain why V1 borrow/return was structurally unstable.
**Step 2:** Explain why Lua + requestId solved the write-path problem.
**Step 3:** Connect the argument to `case-005` tests without overstating scope.

### Task 3: Add blog draft for Part 2 (selection policy)

**Files:**
- Create: `docs/blog-02-how-proxy-v2-changed.md`

**Step 1:** Explain limits of app-based first-fit.
**Step 2:** Explain why `pool:{domain}` and `cache:proxies:{domain}` were needed.
**Step 3:** Explain round-robin/cache/fallback and why policy differs from consistency.

### Task 4: Add series index

**Files:**
- Create: `docs/proxy-v1-v2-series-index.md`

**Step 1:** Summarize the role of each document.
**Step 2:** Show reading order and link the architecture doc.
