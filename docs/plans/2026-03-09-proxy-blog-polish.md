# Proxy Blog Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Polish the public blog drafts so part 1 reads more cleanly and part 2 places the minimal repro link where it best supports the argument.

**Architecture:** Keep the current 2-part structure and only refine presentation. Part 1 gets a tighter opening and clearer section headings. Part 2 keeps its structure but moves the `case-005` reference closer to the consistency discussion so the link supports the exact claim it proves.

**Tech Stack:** Markdown docs, local file edits

---

### Task 1: Tighten Part 1 opening and headings

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`

**Step 1: Rewrite the introduction**
- Reduce repetition between "왜 이 글을 쓰는가" and the first section.
- Make the opening question-driven: imbalance, blocked proxies, domain-unaware model.

**Step 2: Rename section headings for scanability**
- Replace generic headings with shorter, stronger ones.
- Keep structure: first-fit, domain blindness, 1:1 binding, partial fixes.

**Step 3: Re-read for flow**
- Ensure each heading naturally leads to the next.

### Task 2: Reposition case-005 reference in Part 2

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Move the minimal repro reference**
- Put the `case-005` link near the section that explains requestId, TTL, and atomic write-path.
- Avoid leaving it as an isolated late appendix-style note.

**Step 2: Clarify the role of the repro**
- State explicitly that it is not a full V2 reproduction.
- State exactly which claim it supports.

**Step 3: Re-read for narrative order**
- Ensure the order is: structure change -> consistency consequence -> supporting repro.

### Task 3: Verify edited docs

**Files:**
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`
- Review: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-02-how-proxy-v2-changed.md`

**Step 1: Read both docs**
- Confirm the new intro and headings are in place.
- Confirm the `case-005` reference sits near consistency discussion.

**Step 2: Run grep checks**
Run: `rg -n "case-005-proxy-lua-minimal-repro|왜 이 글을 쓰는가|first-fit|정합성" docs/blog-01-why-proxy-v2-was-needed.md docs/blog-02-how-proxy-v2-changed.md`
Expected: hits show the updated structure and repro reference.
