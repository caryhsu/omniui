## Why

UI tests against JavaFX apps are susceptible to timing issues: asynchronous state changes (e.g., a status label updating after a button click) may not have settled by the time the next assertion runs. Currently callers must insert manual `time.sleep()` calls, which are fragile and add unnecessary wait time. A built-in retry mechanism lets tests express intent ("wait until this assertion passes") cleanly.

## What Changes

- Add `retry(fn, *, times=3, delay=0.5, exceptions=(AssertionError,))` static/instance helper on `OmniUIClient`
- Retries `fn()` up to `times` attempts, sleeping `delay` seconds between attempts
- Catches `AssertionError` (from `verify_*` methods) and `ActionResult` failures (ok=False) by default
- Re-raises the last exception if all attempts fail, with attempt count in the message
- Also expose as a module-level utility `omniui.retry(fn, ...)` for use without a client instance

## Capabilities

### New Capabilities
- `retry-helper`: `client.retry(fn, times=3, delay=0.5)` — retry a callable until it passes or exhausts attempts

### Modified Capabilities

## Impact

- `omniui/core/engine.py` — add `retry()` method to `OmniUIClient`
- `omniui/__init__.py` — re-export `retry` at package level
- `demo/python/core/retry_demo.py` — new demo
- `demo/python/run_all.py` — add retry_demo to core group
- `tests/test_client.py` — new `RetryHelperTests`
