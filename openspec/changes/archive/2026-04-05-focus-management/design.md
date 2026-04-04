## Context

OmniUI currently supports click, type, press_key, and hover actions. There is no way to programmatically move keyboard focus to a specific node or verify which node currently holds focus. JavaFX provides `node.requestFocus()` to set focus and `node.isFocused()` to read it; both are synchronous and reliable.

## Goals / Non-Goals

**Goals:**
- `focus(id=...)` — move focus to a node via `requestFocus()`
- `get_focused()` — return the currently focused node's fxId (or handle if no fxId)
- `tab_focus(times=N, shift=False)` — thin wrapper over `press_key("Tab")` / `press_key("Shift+Tab")`
- `verify_focused(id=...)` — assertion helper built on `get_focused()`

**Non-Goals:**
- Focus traversal policy customisation
- Multi-window focus (out of scope; `get_focused` targets the primary scene)

## Decisions

**`focus` action → `node.requestFocus()` via reflection**
`requestFocus()` is a public API on `Node`. No reflection needed — call directly after resolving the node. Returns `{"action":"focus","status":"ok"}` always (JavaFX ignores the call if the node is not focusable; we do not treat this as an error).

**`get_focused` action → walk Scene.focusOwner**
Java side: `scene.getFocusOwner()` returns the focused node. We extract its `fx:id` (via `getProperties()`) and node type, then return them. No selector needed.

**`tab_focus` is Python-only**
Implemented as `press_key("Tab")` × N (or `"Shift+Tab"` if reverse=True). Zero new Java code.

**`verify_focused` is Python-only assertion**
Calls `get_focused()` and compares fxId. Raises `AssertionError` on mismatch — consistent with other `verify_*` methods.

## Risks / Trade-offs

- `requestFocus()` is a no-op for non-focusable nodes → not surfaced as an error. Callers should use `verify_focused()` afterwards to confirm.
- `getFocusOwner()` can return `null` if no node is focused — returned as `{"fxId": null}`.

## Migration Plan

No breaking changes. New actions and methods are additive.
