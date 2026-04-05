## Context

`ToolBar` in JavaFX is a container that holds `Node` children (usually `Button`, `Separator`, `ToggleButton`). Accessing its items requires calling `getItems()` on the `ToolBar` node, which returns an `ObservableList<Node>`. Each item can be inspected for its type, text, and disabled state. The existing `click(id=...)` already handles clicking any node, so no new click action is needed.

## Goals / Non-Goals

**Goals:**
- `get_toolbar_items(id)` — return metadata list for all items in the toolbar

**Non-Goals:**
- Adding toolbar-specific click (use `click(id=...)`)
- ToolBar items without an `fx:id` (they appear with `id=null` in the result)
- Overflow/popup items (items hidden when toolbar is too narrow)

## Decisions

**D1: Return format**
Return a list of dicts: `[{"fxId": str|null, "text": str, "type": str, "disabled": bool}]`.
`type` is the simple class name (e.g. `"Button"`, `"Separator"`, `"ToggleButton"`).
This matches the pattern used by `get_tabs` which returns a list of tab descriptors.

**D2: No new click primitive**
`click(id="myBtn")` already works for toolbar buttons. Adding a `click_toolbar_item(index=N)` would be redundant and harder to maintain.

## Risks / Trade-offs

- [Items without fx:id] → Included with `fxId: null`; callers can filter by text or index
