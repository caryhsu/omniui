## Context

OmniUI tests currently stop at the first assertion failure. Engineers must re-run repeatedly to discover each additional problem. The soft-assert pattern (familiar from TestNG / pytest-check) lets a block accumulate every failure and report them all at once.

The implementation is pure Python — no Java agent changes are needed. `SoftAssertContext` wraps each action call inside the block and swallows `AssertionError`, collecting them in a list. On exit it re-raises a combined error if anything failed.

## Goals / Non-Goals

**Goals:**
- Context manager `client.soft_assert()` that accumulates `AssertionError` failures
- Module-level `soft_assert()` for use without a client
- Re-raises a single `AssertionError` at block exit with all failure messages
- Works with any callable that raises `AssertionError` (verify_text, verify_focused, custom assertions)

**Non-Goals:**
- Catching non-`AssertionError` exceptions (those still propagate immediately)
- Integration with external assertion libraries (pytest-check, etc.)
- Nested soft-assert contexts

## Decisions

### SoftAssertContext as a class (not a decorator)
Using `with client.soft_assert():` is the most readable pattern for test code. It has clear enter/exit semantics and aligns with how pytest and other frameworks expose soft-assert.

*Alternative considered:* A decorator `@client.soft_assert` — rejected because it forces wrapping entire test functions, not targeted assertion groups.

### Accumulate AssertionError only
Only `AssertionError` is swallowed; all other exceptions propagate immediately. This matches the contract of Python's `assert` statement and the `verify_*` helpers.

### Combined message format
Each failure is numbered and includes the original message. The combined `AssertionError` message starts with `"N assertion(s) failed:"` for easy parsing.

## Risks / Trade-offs

- [Risk] Users may wrap broad blocks and miss non-assertion errors that escape. → Mitigation: document that only `AssertionError` is caught.
- [Risk] Ordering of failures may be confusing if interleaved with side effects. → Mitigation: failures are reported in the order they occurred.
