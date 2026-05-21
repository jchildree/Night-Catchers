---
name: tdd
description: Use when implementing any feature or bugfix, before writing implementation code. Also triggers on mentions of "red-green-refactor", "TDD", "test first", integration tests, or test-first development.
---

# Test-Driven Development

## Philosophy

**Core principle:** Tests verify behavior through public interfaces, not implementation details. Code can change entirely; tests shouldn't.

**Good tests** are integration-style — they exercise real code through public APIs, describe *what* the system does, and survive refactors. If you rename an internal function and tests break, those tests were testing implementation, not behavior.

**If you didn't watch the test fail, you don't know if it tests the right thing.**

**Violating the letter of these rules is violating the spirit.**

See [tests.md](tests.md) for examples and [mocking.md](mocking.md) for mocking guidelines.

---

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

Write code before the test? Delete it. Start over.

**No exceptions:**
- Don't keep it as "reference"
- Don't "adapt" it while writing tests
- Don't look at it
- Delete means delete

---

## Anti-Pattern: Horizontal Slices

**DO NOT write all tests first, then all implementation.** This produces tests that verify *imagined* behavior, not *actual* behavior. You outrun your headlights and commit to test structure before understanding the implementation.

```
WRONG (horizontal):
  RED:   test1, test2, test3, test4, test5
  GREEN: impl1, impl2, impl3, impl4, impl5

RIGHT (vertical tracer bullets):
  RED→GREEN: test1→impl1
  RED→GREEN: test2→impl2
  RED→GREEN: test3→impl3
```

Each cycle responds to what you learned from the previous one.

---

## Red-Green-Refactor

### RED — Write Failing Test

One behavior. Clear name. Real code (no mocks unless unavoidable).

**Good:**
```typescript
test('retries failed operations 3 times', async () => {
  let attempts = 0;
  const operation = () => {
    attempts++;
    if (attempts < 3) throw new Error('fail');
    return 'success';
  };
  const result = await retryOperation(operation);
  expect(result).toBe('success');
  expect(attempts).toBe(3);
});
```

**Bad:**
```typescript
test('retry works', async () => {
  const mock = jest.fn()
    .mockRejectedValueOnce(new Error())
    .mockResolvedValueOnce('success');
  await retryOperation(mock);
  expect(mock).toHaveBeenCalledTimes(3); // tests mock, not code
});
```

### Verify RED — MANDATORY. Never skip.

```bash
npm test path/to/test.test.ts
```

Confirm:
- Test fails (not errors)
- Failure message is expected
- Fails because feature is missing, not a typo

**Test passes immediately?** You're testing existing behavior. Fix the test.

### GREEN — Minimal Code

Write the simplest code to pass. No added features, no refactoring other code, no "flexibility" that wasn't asked for.

### Verify GREEN — MANDATORY.

Run all tests. Confirm the new test passes and no regressions.

### REFACTOR

After green only. Remove duplication, improve names, extract helpers. Never add behavior. Run tests after each step.

---

## Workflow

### 1. Planning

Before writing any code:
- Confirm which behaviors to test (prioritize critical paths)
- Design interfaces for testability — see [interface-design.md](interface-design.md)
- Identify [deep modules](deep-modules.md) opportunities (small interface, deep implementation)
- List behaviors to test — not implementation steps
- Get user approval on the plan

### 2. Tracer Bullet

Write ONE test confirming ONE thing end-to-end. This proves the path works before expanding.

### 3. Incremental Loop

For each remaining behavior: one test → minimal code → pass. Don't anticipate future tests.

### 4. Refactor

After all tests pass. See [refactoring.md](refactoring.md).

---

## Good Tests

| Quality | Good | Bad |
|---------|------|-----|
| **Minimal** | One thing. "and" in name? Split it. | `test('validates email and domain and whitespace')` |
| **Clear** | Name describes behavior | `test('test1')` |
| **Behavioral** | Uses public interface only | Tests private methods or mock internals |

---

## Common Rationalizations

| Excuse | Reality |
|--------|---------|
| "Too simple to test" | Simple code breaks. Test takes 30 seconds. |
| "I'll test after" | Tests after = "what does this do?" Tests first = "what should this do?" |
| "Tests after achieve the same goals" | Tests-after are biased by your implementation. You verify what you remember, not what's required. |
| "Already manually tested" | Ad-hoc ≠ systematic. No record, can't re-run. |
| "Deleting X hours is wasteful" | Sunk cost fallacy. Unverified code is technical debt. |
| "Keep as reference, write tests first" | You'll adapt it. Delete means delete. |
| "TDD will slow me down" | TDD is faster than debugging in production. |
| "Need to explore first" | Fine. Throw away the exploration. Start with TDD. |
| "Test is hard = design unclear" | Listen to the test. Hard to test = hard to use. |

---

## Red Flags — STOP and Start Over

- Code before test
- Test passes immediately (you're testing existing behavior — fix the test)
- "I already manually tested it"
- "Tests after achieve the same purpose"
- "It's about spirit not ritual"
- "Keep as reference" or "adapt existing code"
- "Already spent X hours, deleting is wasteful"
- "TDD is dogmatic, I'm being pragmatic"
- "This is different because..."

**All of these mean: Delete code. Start over.**

---

## Verification Checklist

Before marking work complete:
- [ ] Every new function/method has a test
- [ ] Watched each test fail before implementing
- [ ] Each test failed for the expected reason (feature missing, not typo)
- [ ] Wrote minimal code to pass each test
- [ ] All tests pass
- [ ] Output pristine (no errors, warnings)
- [ ] Tests use real code (mocks only if unavoidable)
- [ ] Edge cases and errors covered

Can't check all boxes? You skipped TDD. Start over.

---

## When Stuck

| Problem | Solution |
|---------|----------|
| Don't know how to test | Write the wished-for API. Write the assertion first. Ask your partner. |
| Test too complicated | Design too complicated. Simplify the interface. |
| Must mock everything | Code too coupled. Use dependency injection. |
| Test setup is huge | Extract helpers. Still complex? Simplify the design. |

---

## Bug Fixes

Write a failing test reproducing the bug. Follow the TDD cycle. The test proves the fix and prevents regression.

Never fix bugs without a test.

---

## Testing Anti-Patterns

When writing mocks or test utilities, see [../test-driven-development/testing-anti-patterns.md](../test-driven-development/testing-anti-patterns.md) to avoid:
- Testing mock behavior instead of real behavior
- Adding test-only methods to production classes
- Mocking without understanding dependencies
