## Context

`performOnFxThread()` already has a pattern for simple property getters (e.g. `get_text`, `get_tooltip`). Both new actions follow the same pattern: resolve node → call method via `safeInvoke()` → return result.

- `node.getStyle()` → returns a `String` (inline style, e.g. `"-fx-text-fill: red;"`) or `""` if none set
- `node.getStyleClass()` → returns an `ObservableList<String>` which must be converted to a plain `List`/JSON array

## Goals / Non-Goals

**Goals:**
- `get_style(**selector)` returns the node's inline style string (empty string if none)
- `get_style_class(**selector)` returns a JSON array of CSS class name strings

**Non-Goals:**
- Reading computed/effective styles (JavaFX doesn't expose these via public API)
- Setting styles (use existing `type`/`click` actions to trigger state changes)
- Reading pseudo-class states (e.g. `:hover`, `:focused`) — separate future feature

## Decisions

### D1: `get_style` returns raw inline style string
`node.getStyle()` already returns `""` when no inline style is set — no null check needed.

### D2: `get_style_class` converts ObservableList to JSON array
`getStyleClass()` returns `ObservableList<String>`. Call `.toArray()` or iterate and collect to a `List<String>`, then serialize as a JSON array. The agent's `ActionResult.success()` already accepts `Object` as value — pass a `List<String>`.

### D3: Both in `performOnFxThread()` — same branch as other getters
No special threading required.

## Risks / Trade-offs

- **Inline style vs stylesheet**: `getStyle()` only returns inline styles set via `node.setStyle(...)`. Styles coming from external CSS files are NOT returned. Document this limitation clearly.
- **ObservableList serialization**: Must convert to a plain `List` before passing to `ActionResult` to avoid serialization issues with JavaFX observable types.
