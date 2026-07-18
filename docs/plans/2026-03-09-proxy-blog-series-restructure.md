# Proxy Blog Series Restructure Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reframe the proxy blog series so part 1 explains why V2 was needed and part 2 explains how V2 changed the structure and improved consistency.

**Architecture:** Keep the architecture reference document intact, rewrite the two blog drafts around the new narrative order, and update the series index to match the renamed themes. Preserve Mermaid-heavy architecture context and `case-005` as supporting evidence rather than the main title of part 1.

**Tech Stack:** Markdown documentation, Mermaid references, local file review, git status verification

---

### Task 1: Rewrite part 1 around V2 motivation

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`

**Step 1: Replace the title and introduction**
Reframe part 1 as the failure of V1 selection policy and proxy model.

**Step 2: Move in the first-fit/domain/1:1 binding narrative**
Make part 1 explain front bias, domain blindness, and long-held bindings.

**Step 3: End by motivating V2**
Close with why a different pool model and structure were necessary, pointing to part 2.

### Task 2: Rewrite part 2 around V2 structure

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Replace the title and framing**
Reframe part 2 as the structural answer: domain pool, round-robin, cache/fallback.

**Step 2: Bring consistency in as a structural consequence**
Explain why borrowed mark, requestId, Lua, and TTL fit inside the new structure instead of leading the series.

**Step 3: Keep the close on single-writer and operational safety**
Make part 2 land on operational ownership and stability.

### Task 3: Update series index

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/proxy-v1-v2-series-index.md`

**Step 1: Update the titles and reading order**
Match the new part 1 / part 2 framing.

**Step 2: Keep architecture and repro links**
Retain the architecture document and `case-005` as supporting references.

### Task 4: Verify docs

**Files:**
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/proxy-v1-v2-series-index.md`

**Step 1: Inspect final content**
Run `sed -n` for the three documents and confirm the new narrative order.

**Step 2: Check file state**
Run `git status --short` for the edited docs.
