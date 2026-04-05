## Why

JavaFX `ToolBar` is a common widget for quick-action buttons, yet OmniUI has no dedicated API for it. Tests that need to click toolbar buttons currently require knowing the button's `fx:id` directly. Adding `get_toolbar_items()` gives test authors visibility into what's available, improving discoverability and making toolbar interactions explicit.

## What Changes

- Add `get_toolbar_items(id)` — return a list of toolbar item descriptors (fxId, text, type, disabled)
- Clicking toolbar buttons continues to use the existing `click(id=...)` — no new click primitive needed

## Capabilities

### New Capabilities

- `toolbar-operations`: `get_toolbar_items` — query the items inside a `ToolBar` node by its fxId

### Modified Capabilities

## Impact

- `java-agent/…/ReflectiveJavaFxTarget.java` — new `get_toolbar_items` action in `performOnFxThread`
- `omniui/core/engine.py` — new `OmniUIClient.get_toolbar_items()` method
- `tests/test_client.py` — new `ToolBarTests`
- `demo/python/core/toolbar_demo.py` — new demo using existing advanced-app ToolBar
- `demo/python/run_all.py` — add toolbar_demo to advanced group
