## Context

OmniUI is a JavaFX UI automation framework. Python client methods like `verify_text()` raise `AssertionError` on mismatch, and action methods return `ActionResult` with `ok=False` on failure. There is currently no built-in retry loop.

## Goals / Non-Goals

**Goals:**
- `client.retry(fn, times=3, delay=0.5)` — call `fn()` repeatedly until it succeeds or runs out of attempts
- Catches `AssertionError` by default (covers all `verify_*` methods)
- Optionally catches `ActionResult` failures via a flag
- Re-raises last exception with clear message showing attempt count
- Module-level alias `omniui.retry` for convenience

**Non-Goals:**
- Async/await support
- Timeout-based retry (time budget instead of attempt count) — keep simple for now
- Modifying Java agent

## Decisions

**Standalone function, not decorator**
The ROADMAP shows `@client.retry(...)` decorator syntax, but a plain callable `client.retry(lambda: ..., times=3)` is simpler, more testable, and works with multi-line blocks via a nested function. A decorator factory adds complexity without real benefit for UI test use-cases.

**`exceptions` tuple parameter**
Defaults to `(AssertionError,)`. Callers can pass `exceptions=(AssertionError, RuntimeError)` to widen scope. For `ActionResult` failures, we inspect the return value: if `fn()` returns an `ActionResult` with `ok=False`, treat it as a retry trigger.

**Delay between attempts, not before first**
First attempt runs immediately. Sleep only happens between attempts.

**Instance method + module-level re-export**
`client.retry(fn)` works for users who have a client. `from omniui import retry; retry(fn)` works for standalone use. Both are the same function.

## Risks / Trade-offs

- Swallowing exceptions during intermediate attempts hides transient errors — acceptable for UI test polling.
- Long `delay * times` values can slow tests — user's responsibility to tune.

## Migration Plan

Purely additive. No breaking changes.
