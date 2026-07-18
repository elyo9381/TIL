# Proxy Blog V1 Diagram Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a compact V1 diagram to blog post 1 so the reader can see that V1 tracked proxy ownership but did not model domain-specific availability.

**Architecture:** Insert one short Mermaid diagram near the first explanation of V1 borrow behavior. Follow it with a brief paragraph that explains the modeling gap in plain language and ties directly back to the post's argument.

**Tech Stack:** Markdown docs, Mermaid, apply_patch, shell verification

---

### Task 1: Add a compact V1 diagram and explanation

**Files:**
- Modify: `/Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`

**Step 1: Insert a Mermaid diagram after the V1 pseudocode**
- Include only `worker agent`, `legacy coordinator`, `ProxyPool`, and `Redis proxy:{ip}` state.
- Show `active owner map` and `last release map` only.

**Step 2: Add a short explanatory paragraph under the diagram**
- State that V1 could express occupancy and reuse delay, but not domain-specific blocked/available state.

**Step 3: Verify flow**
Run: `sed -n '25,65p' /Users/yowon/study/til/TIL-case004-minimal-v1-v2/docs/blog-01-why-proxy-v2-was-needed.md`
Expected: pseudocode -> compact diagram -> short explanation -> existing numbered explanation.
