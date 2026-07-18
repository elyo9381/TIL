# Proxy Blog Post 2 Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Tighten the second proxy V2 blog post by simplifying the title and unifying the domain-pool terminology in the conclusion.

**Architecture:** Keep the section structure intact. Adjust only three reader-facing wording points so the post uses one stable vocabulary from top to bottom.

**Tech Stack:** Markdown docs, apply_patch, shell verification

---

### Task 1: Unify title and domain-pool wording

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Simplify the title**
- Reduce the title to `도메인별 기준 풀` and `상태 분리`.

**Step 2: Use one Korean term for domain pool**
- First mention can be `도메인별 기준 풀(domain pool)`.
- Later mentions should be `도메인별 기준 풀`.

**Step 3: Replace the closing summary wording**
- Remove `1차 스냅샷 경로` and use `할당용 순환 큐` instead.

**Step 4: Verify flow**
Run: `sed -n '1,170p' /Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`
Expected: title, opening, and closing use one consistent vocabulary.
