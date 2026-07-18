# Case-004 Simplify V1/V2 Design

**Goal**
- Reduce `case-004` from a three-profile experimental module to a two-profile repro focused on `v1-legacy` vs `v2-target`.
- Remove `v1-p4` from code, tests, API surface, README, and blog draft.
- Keep the module focused on the three behaviors that matter for the blog: duplicate lease, orphan lease, idempotency mismatch.

**Scope**
- In scope: enum/profile cleanup, service cleanup, test cleanup, docs cleanup.
- Out of scope: deeper repository split, latency metric redesign, Redis key redesign.

**Decision**
- Keep `v1-p3` behavior as the single legacy profile and rename it to `v1-legacy`.
- Keep `v2-target` as is.
- Preserve existing scenario runner and repository internals for now, but stop exposing the extra V1 variant.

**Why**
- Current code carries more historical detail than the blog needs.
- `v1-p4` does not currently provide a distinct, test-proven lesson.
- Two profiles make the API, tests, and explanation materially easier to read.
