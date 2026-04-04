## Why

JavaFX `ScrollPane` is one of the most common container widgets and is used to host tables, lists, forms, and long content that cannot fit in a single visible area. Without scroll support, automation scripts cannot interact with elements that are off-screen or reach the bottom of a long list. This is the last remaining High Priority item in the ROADMAP.

## What Changes

- Add `scroll_to(**selector)` to the Python client — scrolls a `ScrollPane` or scrollable container so the target node is visible
- Add `scroll_by(delta_x, delta_y, **selector)` to the Python client — scrolls a `ScrollPane` by a relative pixel/unit offset
- Java agent: add `scroll_to` and `scroll_by` action handlers in `ReflectiveJavaFxTarget`
- Demo app: add a scrollable list to the login demo or a dedicated scroll panel
- Demo: `scroll_demo.py` smoke test

## Capabilities

### New Capabilities

- `scroll-support`: Python and Java agent support for `scroll_to` (scroll to make node visible) and `scroll_by` (relative scroll offset) on JavaFX `ScrollPane` nodes

### Modified Capabilities

- `python-automation-client`: add `scroll_to` and `scroll_by` methods to the public API surface

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new cases in `performOnFxThread()`
- `omniui/core/engine.py` — two new public methods
- `demo/javafx-login-app/src/main/java/dev/omniui/demo/LoginDemoApp.java` — add scrollable panel
- `demo/python/scroll_demo.py` — new demo
- `demo/python/run_all.py` — add scroll_demo entry
- `docs/api/python-client.md`, `README.md`, `README.zh-TW.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- Java agent jlink rebuild required (Java change)
