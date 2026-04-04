## Why

Keyboard shortcuts are a fundamental interaction pattern in desktop applications. Many JavaFX applications rely on `KeyEvent` handlers (e.g., `Escape` to cancel, `Enter` to confirm, `Ctrl+S` to save, `Ctrl+Z` to undo) that cannot be triggered by existing `click` or `type` actions. A dedicated `press_key()` action is needed to cover these scenarios.

## What Changes

- Add `press_key` action to the Java agent (`ReflectiveJavaFxTarget`) that fires `KeyEvent.KEY_PRESSED` + `KeyEvent.KEY_RELEASED` on the focused node (or scene root if no selector given)
- Key string format follows Playwright convention: `"Control+C"`, `"Shift+Tab"`, `"Enter"`, `"Escape"` — case-insensitive, with `Ctrl` accepted as alias for `Control`, `Win`/`Meta` interchangeable
- Add `press_key(key, **selector)` method to the Python client (`engine.py`)
- Add a demo script `keyboard_shortcuts_demo.py` and register it in `run_all.py`
- Update README (EN + zh-TW), `docs/api/python-client.md`, ROADMAP

## Capabilities

### New Capabilities
- `keyboard-shortcuts`: Dispatches `KeyEvent.KEY_PRESSED` + `KeyEvent.KEY_RELEASED` on a target JavaFX node or the focused scene node; supports modifier key combinations with a human-readable string format

### Modified Capabilities
- `python-automation-client`: Add `press_key(key, **selector)` method requirement

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new case + handler
- `omniui/core/engine.py` — new `press_key()` method
- `demo/python/keyboard_shortcuts_demo.py` — new demo
- `demo/python/run_all.py` — add keyboard demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- jlink image rebuild required after Java agent change
