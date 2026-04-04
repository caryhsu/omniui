## Why

Test scripts often need to verify or manipulate clipboard content — for example, checking that "Copy" buttons work, or pre-seeding input fields via paste. JavaFX provides `Clipboard.getSystemClipboard()` which the agent can expose as first-class actions.

## What Changes

- New `copy(id=...)` action — triggers Ctrl+C on a node (select-all + copy)
- New `paste(id=...)` action — triggers Ctrl+V on a node
- New `get_clipboard()` action — reads current system clipboard text content
- New `set_clipboard(text=...)` action — writes text to system clipboard (useful for test setup)

## Capabilities

### New Capabilities

- `clipboard-operations`: Read and write system clipboard; trigger copy/paste on UI nodes

### Modified Capabilities

(none)

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — new `get_clipboard` and `set_clipboard` actions (first-layer switch, no node resolution needed)
- `omniui/core/engine.py` — `copy()`, `paste()`, `get_clipboard()`, `set_clipboard()` methods on `OmniUIClient`
- No new dependencies
- New demo `demo/python/core/clipboard_demo.py`
- New tests in `tests/test_client.py`
