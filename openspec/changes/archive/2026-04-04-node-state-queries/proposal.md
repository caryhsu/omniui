## Why

Tests often need to assert whether a UI node is currently visible or interactable without performing an action on it. OmniUI currently exposes `visible` and `enabled` values inside node discovery metadata, but there are no dedicated convenience methods — callers must use `find()` and manually inspect the result. This change adds `is_visible()` and `is_enabled()` as first-class Python client methods, consistent with how `is_visited()` and `get_expanded()` are already exposed.

## What Changes

- Add `is_visible(**selector) -> bool` to `engine.py`
- Add `is_enabled(**selector) -> bool` to `engine.py`
- Both read from the `visible` / `enabled` fields already present in node discovery metadata — **no Java agent changes required**
- Return `False` when the node is not found (Playwright-style, consistent with existing `is_visited()` behavior)
- Add a demo script `node_state_demo.py` to verify both methods
- Add both methods to `run_all.py`

## Capabilities

### New Capabilities

- `node-state-queries`: Python convenience methods `is_visible` and `is_enabled` for checking node state without performing an action

### Modified Capabilities

- `python-automation-client`: New methods added to the client API surface

## Impact

- `omniui/core/engine.py` — 2 new methods
- `demo/python/node_state_demo.py` — new demo script
- `demo/python/run_all.py` — add new demo
- No Java agent changes, no jlink rebuild needed
