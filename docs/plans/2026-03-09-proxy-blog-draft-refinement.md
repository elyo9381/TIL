# Proxy Blog Draft Refinement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Separate the proxy blog series into a consistency-focused part 1 and a selection-policy-focused part 2.

**Architecture:** Keep the existing architecture reference document intact, narrow `blog-01` to Redis consistency and Lua/requestId, and let `blog-02` carry first-fit bias, domain-pool motivation, and round-robin/cache/fallback discussion. No code changes are needed.

**Tech Stack:** Markdown documentation, Mermaid diagrams, local git diff verification

---

### Task 1: Refine part 1 draft

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`

**Step 1: Remove selection-policy material from the opening**
Delete the first-fit, idx, and domain-blindness sections that belong in part 2.

**Step 2: Rebuild the introduction around consistency**
Keep the structure focused on read-modify-write, duplicate borrow, orphan/retry mismatch, and Lua + requestId.

**Step 3: Reconnect the minimal repro**
Keep `case-005` as the supporting evidence for the two consistency scenes only.

### Task 2: Refine part 2 draft

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Strengthen the first-fit explanation**
Preserve generalized pseudocode and make the front-of-list bias explicit without overstating Redis Set ordering guarantees.

**Step 2: Keep idx/domain sections as policy discussion**
Explain why service-local pointer metadata was not enough and why domain-aware pools were needed.

**Step 3: Keep the conclusion tied to pool/domain/cache/fallback/single-writer**
Ensure part 2 closes on operating-structure choices rather than Lua consistency.

### Task 3: Verify the drafts

**Files:**
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Inspect final content**
Run `sed -n` on both files to confirm section boundaries.

**Step 2: Review the diff**
Run `git diff -- docs/blog-01-why-proxy-v2-was-needed.md docs/blog-02-how-proxy-v2-changed.md` and confirm that only the intended narrative split changed.
