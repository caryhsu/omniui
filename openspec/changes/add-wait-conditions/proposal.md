## Why

JavaFX UIs are asynchronous — after triggering an action, text/visibility/state changes may not be immediately available. Without wait helpers, test scripts must use `time.sleep()` with arbitrary delays, making tests slow and fragile. Poll-based wait conditions solve this by retrying until the expected state is reached or a timeout expires.

## What Changes

- Add `wait_for_text(id, expected, timeout)` — polls until a node's text matches `expected`
- Add `wait_for_visible(id, timeout)` — polls until `is_visible()` returns `True`
- Add `wait_for_enabled(id, timeout)` — polls until `is_enabled()` returns `True`
- Add `wait_for_node(id, timeout)` — polls until a node with the given `id` appears in discovery
- Add `wait_for_value(id, expected, timeout)` — polls until `get_text()` returns `expected` (alias for `wait_for_text` with explicit `id=`)

All methods are **pure Python** poll loops — no Java agent changes required. They raise `TimeoutError` on failure.

## Capabilities

### New Capabilities
- `wait-conditions`: Poll-based Python wait helpers that block until a UI state condition is met or a timeout expires

### Modified Capabilities
- `python-automation-client`: New wait methods added to the public API surface

## Impact

- `omniui/core/engine.py` — 5 new methods
- `demo/python/` — new demo script `wait_conditions_demo.py`
- `demo/python/run_all.py` — add new demo
- `README.md` / `README.zh-TW.md` — document new methods
- `docs/api/python-client.md` — new Wait Conditions section
- `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark wait conditions `[x]`
