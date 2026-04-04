## Context

JavaFX exposes `Clipboard.getSystemClipboard()` for reading/writing text. For copy/paste on a node, the cleanest approach is to fire `KeyEvent` (Ctrl+A then Ctrl+C, or Ctrl+V) rather than calling JavaFX clipboard API directly — this exercises the real application code path.

`get_clipboard` and `set_clipboard` are agent-level actions that do not require a node. `copy` and `paste` are convenience wrappers over `press_key` on the Python side — they do not need new Java actions.

## Goals / Non-Goals

**Goals:**
- `get_clipboard()` — return current clipboard text via Java agent
- `set_clipboard(text)` — write text to clipboard via Java agent (test setup)
- `copy(id=...)` — select-all + Ctrl+C on a node (Python convenience using `press_key`)
- `paste(id=...)` — Ctrl+V on a node (Python convenience using `press_key`)

**Non-Goals:**
- Non-text clipboard content (images, files)
- OS-level clipboard history or monitoring

## Decisions

### copy/paste as Python-only wrappers
`copy(id=...)` issues `press_key("Control+A", id=...)` then `press_key("Control+C", id=...)`. `paste(id=...)` issues `press_key("Control+V", id=...)`. No new Java action needed — reuses existing `press_key` dispatch.

*Alternative:* New Java actions `clipboard_copy` / `clipboard_paste` — rejected as unnecessary given `press_key` already handles key events correctly.

### get_clipboard / set_clipboard as Java actions (first layer)
These need access to `Clipboard.getSystemClipboard()` which requires JavaFX thread awareness but no node lookup. They go in the first-layer switch (like `get_focused`).

## Risks / Trade-offs

- [Risk] Clipboard content is OS-global; parallel tests may interfere. → Mitigation: document that clipboard tests should not run in parallel.
- [Risk] `set_clipboard` modifies user's real clipboard during test. → Mitigation: document; acceptable for automation use.
