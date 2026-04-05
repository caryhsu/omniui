## Context

JavaFX exposes all open windows via `javafx.stage.Stage.getWindows()` (static, returns `ObservableList<Window>`). Each `Stage` has `getTitle()`, `setMaximized()`, `setIconified()`, `toFront()`, `setWidth/Height/X/Y`, `getWidth/Height/getX/getY`, `isMaximized()`, `isIconified()`. Window operations must run on the FX Application Thread.

The existing agent dispatches actions based on a node selector; window actions don't require a scene node — they operate on the Stage directly. We handle this by routing window actions **before** the normal node-lookup path, at the top of `perform()`.

## Goals / Non-Goals

**Goals:**
- Enumerate windows by title
- Focus, maximize, minimize, restore
- Set/get size and position

**Non-Goals:**
- Closing windows (risk of test instability)
- Waiting for window to appear (use existing `wait_until` pattern)
- Multi-monitor DPI-aware coordinates

## Decisions

**Stage lookup by title:** `Stage.getWindows()` → filter by `getTitle().equals(title)`. If `title=null`, use the first/primary stage. Title must be unique — document this.

**Top-level dispatch:** Window actions are detected by action name prefix `get_windows` / `focus_window` / etc. before the selector resolution in `perform()`, to avoid "no node found" failures.

**`get_windows` returns list of title strings:** Simple and sufficient for test assertion. Handle includes title.

**Geometry units:** Raw JavaFX pixels (same as the app coordinate system). No DPI scaling applied by the agent.

## Risks / Trade-offs

- [Risk] Window not found by title → return `reason="window_not_found"`
- [Risk] `minimize_window` may not work in headless/CI environments → document limitation
- [Risk] `Stage.getWindows()` includes non-Stage `Window` instances (e.g. PopupWindow) → filter to `Stage` class only via `getSimpleName().equals("Stage")`
