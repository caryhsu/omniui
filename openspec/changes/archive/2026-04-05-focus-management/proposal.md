## Why

JavaFX applications rely heavily on keyboard navigation and focus state for accessibility and form workflows. Currently OmniUI has no way to programmatically move focus or verify which node is focused, making it impossible to test Tab-key traversal and focus-dependent UI behaviours.

## What Changes

- Add `focus(id=...)` — programmatically focus a node (calls `node.requestFocus()`)
- Add `tab_focus(times=1)` — simulate Tab/Shift+Tab key navigation (reuses existing `press_key`)
- Add `get_focused()` — return the handle/fxId of the currently focused node
- Add `verify_focused(id=...)` — assert that the named node is currently focused

## Capabilities

### New Capabilities
- `focus-management`: Focus control and verification — `focus()`, `tab_focus()`, `get_focused()`, `verify_focused()`

### Modified Capabilities

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — new `"focus"` and `"get_focused"` action cases
- `omniui/core/engine.py` — new `focus()`, `tab_focus()`, `get_focused()`, `verify_focused()` methods
- `demo/python/core/focus_demo.py` — new demo using core-app login form
- `tests/test_client.py` — new FocusManagementTests
