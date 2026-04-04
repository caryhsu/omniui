## Context

`performOnFxThread()` in `ReflectiveJavaFxTarget` handles getter actions like `get_text`, `get_value`, `get_progress`. Each reads a node property via reflection and returns the value in the response JSON. Adding `get_tooltip` follows exactly the same pattern: resolve the node, call `getTooltip()`, then call `getText()` on the result.

JavaFX `Tooltip` is a separate object attached to a node via `node.setTooltip(Tooltip)`. `node.getTooltip()` returns `null` if no tooltip is set.

## Goals / Non-Goals

**Goals:**
- `get_tooltip(**selector)` reads the text of a node's `Tooltip`
- Returns `selector_not_found` if the node doesn't exist
- Returns an empty string (or a clear signal) if the node exists but has no tooltip

**Non-Goals:**
- Triggering the tooltip to appear visually (that requires hover)
- Reading tooltip style or graphics
- Setting a tooltip

## Decisions

### D1: Return empty string for no-tooltip case
If `node.getTooltip()` returns `null`, return `""` (empty string) rather than an error. This lets callers assert `result.value == ""` to confirm no tooltip is set, without a separate "no tooltip" result code.

### D2: Implement in `performOnFxThread()` alongside other getters
Same branch as `get_text`, `get_value`, etc. Consistent pattern, minimal code.

### D3: Reflection chain — `getTooltip()` then `getText()`
Both `Tooltip` and `Node` are accessible at runtime. Use `ReflectiveJavaFxSupport.invoke(node, "getTooltip")` then `invoke(tooltip, "getText")`.

## Risks / Trade-offs

- **Null tooltip**: Handled by D1 — returns `""`.
- **Tooltip set but text is null**: `getText()` could return `null`; guard with null check, return `""`.
- **Tooltip not yet shown**: `getTooltip()` returns the tooltip object regardless of visibility — no issue.
