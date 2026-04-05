## Why

Multi-window test scenarios require enumerating open windows, switching focus, and controlling window state (maximize, minimize, resize, reposition). Currently OmniUI has no window-level API — all actions assume a single implicit window. Adding Stage management unlocks tests that open dialogs, secondary windows, or perform layout validation.

## What Changes

- `get_windows()` — list all open Stage titles and handles
- `focus_window(title=...)` — bring a Stage to front
- `maximize_window(title=...)` / `minimize_window(title=...)` / `restore_window(title=...)` — window state
- `set_window_size(width, height, title=...)` / `set_window_position(x, y, title=...)` — geometry
- `get_window_size(title=...)` / `get_window_position(title=...)` — read geometry
- `is_maximized(title=...)` / `is_minimized(title=...)` — read state

## Capabilities

### New Capabilities

- `window-enumerate`: List all open JavaFX Stage windows
- `window-focus`: Bring a specific window to the foreground
- `window-state`: Maximize, minimize, restore a window; query maximized/minimized state
- `window-geometry`: Read and set window size and position

### Modified Capabilities

## Impact

- `java-agent/…/ReflectiveJavaFxTarget.java` — new top-level action cases (no node lookup needed — windows are found by title via `Stage.getWindows()`)
- `omniui/core/engine.py` — new public methods
- `demo/java/advanced-app/…/AdvancedDemoApp.java` — add "Open second window" button for demo
- `demo/python/advanced/window_demo.py` — new demo script
- `demo/python/run_all.py` — import + Window Demo section
- `tests/test_client.py` — `WindowTests`
