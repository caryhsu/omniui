## ADDED Requirements

### Requirement: retry() retries a callable until success or exhaustion
The system SHALL provide a `retry(fn, *, times=3, delay=0.5, exceptions=(AssertionError,))` function that calls `fn()` up to `times` times. On each attempt, if `fn()` raises an exception in `exceptions` OR returns an `ActionResult` with `ok=False`, the helper sleeps `delay` seconds and tries again. If the final attempt also fails, the last exception is re-raised. If `fn()` returns an `ActionResult` with `ok=False` on the last attempt, an `AssertionError` is raised with a descriptive message.

#### Scenario: Succeeds on first attempt
- **WHEN** `fn()` returns successfully on the first call
- **THEN** `retry()` returns the result immediately without sleeping

#### Scenario: Succeeds on a later attempt
- **WHEN** `fn()` raises `AssertionError` on attempts 1–2 but succeeds on attempt 3
- **THEN** `retry()` returns the result of attempt 3

#### Scenario: All attempts fail with AssertionError
- **WHEN** `fn()` raises `AssertionError` on all `times` attempts
- **THEN** `retry()` re-raises the last `AssertionError`

#### Scenario: All attempts return ActionResult ok=False
- **WHEN** `fn()` returns `ActionResult(ok=False)` on all attempts
- **THEN** `retry()` raises `AssertionError` describing the failure

#### Scenario: Delay between attempts
- **WHEN** retry is needed between attempts
- **THEN** the helper sleeps exactly `delay` seconds between each attempt (not before the first)

### Requirement: retry is available as a client method and module-level function
The system SHALL expose `retry` both as `OmniUIClient.retry(fn, ...)` instance method and as a module-level `omniui.retry(fn, ...)` function. Both SHALL have identical behaviour.

#### Scenario: Use as instance method
- **WHEN** a Python script calls `client.retry(lambda: client.verify_text(id="s", text="ok"), times=3)`
- **THEN** the callable is retried up to 3 times

#### Scenario: Use as module-level function
- **WHEN** a Python script calls `from omniui import retry; retry(fn, times=3)`
- **THEN** the callable is retried up to 3 times with identical behaviour

### Requirement: retry accepts a custom exceptions tuple
The system SHALL allow callers to pass `exceptions=(AssertionError, RuntimeError)` to widen which exception types trigger a retry.

#### Scenario: Custom exception type
- **WHEN** `retry(fn, exceptions=(ValueError,))` is called and `fn()` raises `ValueError`
- **THEN** the helper retries instead of propagating immediately
