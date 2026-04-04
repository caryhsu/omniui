## Why

Some UI elements lack fx:id or stable text selectors. Tests currently have no way to interact with them. Absolute coordinate click lets automation scripts click any screen position directly, bypassing node resolution.

## What Changes

- New `click_at(x, y)` method on `OmniUIClient` — fires a mouse click at absolute screen coordinates within the JavaFX scene
- Java agent: new `click_at` action in the first-layer switch (no node resolution needed)

## Capabilities

### New Capabilities

- `click-at`: Click at absolute (x, y) coordinates within the JavaFX scene window

### Modified Capabilities

(none)

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — new `click_at` action (first-layer switch, uses Robot or MouseEvent at scene coordinates)
- `omniui/core/engine.py` — `click_at(x, y)` method on `OmniUIClient`
- New demo `demo/python/core/click_at_demo.py`
- New tests in `tests/test_client.py`
