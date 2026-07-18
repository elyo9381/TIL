# Case-004 Repository Split Design

## Goal
- Replace the single `Case004RedisRepository` with three focused collaborators so the case-004 code reads as `legacy path`, `target path`, and `shared scenario support`.

## Recommended approach
- `Case004LegacyRedisRepository`: own V1 legacy borrow/count logic and legacy-only keys.
- `Case004TargetRedisRepository`: own V2 Lua borrow/return/count logic and target-only keys.
- `Case004ScenarioRedisSupport`: own seed/reset/clear/run-lock/shared prefix utilities.

This keeps the external API unchanged while removing the current god-repository shape.

## Alternatives considered
1. Keep one repository and only extract private helpers.
- Lowest churn.
- Weak payoff; the public shape stays confusing.

2. Split only V1 and V2, keep scenario/reset/lock inside one of them.
- Less code movement.
- Ownership stays muddy because shared test harness concerns remain mixed with one path.

3. Full split into legacy/target/support.
- Best readability and future explanation value.
- Slightly more wiring churn.

## Chosen design
- Use option 3.
- Keep service/controller/report contract unchanged.
- Preserve existing behavior and use current integration tests as safety net.
- Add one Spring-context test first to make the split contract explicit.
