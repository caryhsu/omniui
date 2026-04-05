## Why

Tests need fine-grained scroll control for standalone `ScrollBar` nodes (not `ScrollPane`). Currently there is no way to read or set a `ScrollBar`'s scroll position programmatically via OmniUI.

## What Changes

- Add `get_scroll_position(id=...)` — returns current value (0.0–1.0) of a `ScrollBar`
- Add `set_scroll_position(id=..., value=...)` — sets value clamped to `[min, max]`

## Capabilities

### New Capabilities

- `scrollbar-get-position`: Read the current scroll position of a `ScrollBar` node
- `scrollbar-set-position`: Set the scroll position of a `ScrollBar` node

### Modified Capabilities

## Impact

- `java-agent/…/ReflectiveJavaFxTarget.java` — new action cases + handler methods
- `omniui/core/engine.py` — two new public methods
- `demo/java/advanced-app/…/AdvancedDemoApp.java` — add a standalone `ScrollBar` widget
- `demo/python/advanced/scrollbar_demo.py` — new demo script
- `demo/python/run_all.py` — import + ScrollBar Demo section
- `tests/test_client.py` — `ScrollBarTests`
