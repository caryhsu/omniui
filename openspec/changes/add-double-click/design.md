## Context

OmniUI's Java agent dispatches JavaFX automation actions via `ReflectiveJavaFxTarget`. The existing `click` action calls `node.fire()`, which fires an `ActionEvent`. Many JavaFX nodes (TreeView, ListView, TableView) respond to double-click via `setOnMouseClicked` handlers that inspect `MouseEvent.getClickCount()`. These handlers are never triggered by `ActionEvent`, so a separate `double_click` action is required.

The agent uses reflection exclusively (no compile-time JavaFX imports) to remain version-agnostic. `ReflectiveJavaFxSupport` provides `invoke()`, `invokeStatic()`, and `loadClass()` helpers that all new code must follow.

## Goals / Non-Goals

**Goals:**
- Fire `MouseEvent.MOUSE_CLICKED` with `clickCount=2` on a node-targeted selector
- Reuse existing selector resolution (`resolve()`) and result patterns
- Add `double_click(**selector)` to the Python client
- Rebuild jlink image and provide a working demo

**Non-Goals:**
- Triple-click or n-click generalization
- Simulating MOUSE_PRESSED / MOUSE_RELEASED sequences (MOUSE_CLICKED alone is sufficient for handlers)
- Robot-based OS-level mouse movement (in-JVM event dispatch is sufficient and faster)

## Decisions

### D1: Fire MOUSE_CLICKED only (not PRESSED + RELEASED + CLICKED sequence)

Most JavaFX double-click handlers use `setOnMouseClicked` and check `clickCount`. Firing `MOUSE_CLICKED` with `clickCount=2` is sufficient. Firing the full press/release/click sequence adds complexity with no practical benefit for automation.

_Alternatives considered_: Fire full 3-event sequence → rejected (unnecessary complexity).

### D2: Construct MouseEvent via reflection using the public 17-argument constructor

`MouseEvent` has a stable public constructor:
```
MouseEvent(EventType, double x, double y, double screenX, double screenY,
           MouseButton, int clickCount,
           boolean shiftDown, boolean controlDown, boolean altDown, boolean metaDown,
           boolean primaryButtonDown, boolean middleButtonDown, boolean secondaryButtonDown,
           boolean synthesized, boolean popupTrigger, boolean stillSincePress, PickResult)
```
Pass `null` for `PickResult` (acceptable for synthesized events). Fire via `Event.fireEvent(EventTarget, Event)` static method using reflection.

_Alternatives considered_: JavaFX Robot API → requires screen position and may fail headlessly; `node.fire()` with clickCount → `fire()` dispatches ActionEvent, ignores clickCount.

### D3: Node center as click coordinates

Use `node.getBoundsInLocal()` to compute the node's center (width/2, height/2), then `node.localToScreen(x, y)` for screen coordinates. This mirrors the `doRightClick()` pattern already in the codebase.

### D4: `double_click` belongs in `performOnFxThread()` (node-targeted)

Unlike `close_app` (top-level, no node), double-click targets a specific node and must be resolved via selector. It goes in `performOnFxThread()` switch, same as `click`.

## Risks / Trade-offs

- **PickResult null**: Some internal JavaFX code may NPE if it dereferences the PickResult. In practice, synthesized events with null PickResult are well-tolerated in JavaFX 11+. → Mitigation: catch Exception broadly and return `double_click_failed`.
- **Headless environments**: `localToScreen` may return null if the node has no scene/window. → Mitigation: fall back to `(0, 0)` for screen coords; the synthesized event still fires but screen coords will be wrong (irrelevant for most handlers).
- **Module access**: The agent runs as a `-javaagent` with `--add-opens` flags already configured for JavaFX internals. Reflection on public `MouseEvent` constructor should not require additional opens.
