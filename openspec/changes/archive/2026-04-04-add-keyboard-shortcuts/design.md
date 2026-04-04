## Context

OmniUI's Java agent dispatches actions via `ReflectiveJavaFxTarget`. The existing `type` action fires `KeyEvent` per character for text input, but does not support modifier key combinations or standalone special keys (Enter, Escape, Tab, F-keys). These are needed to trigger application-level keyboard shortcuts and form navigation.

The agent uses reflection exclusively. `ReflectiveJavaFxSupport` provides `invoke()`, `loadClass()`, and `invokeStatic()`. The `double_click` implementation established the pattern for constructing input events via reflection and firing via `Event.fireEvent()`.

## Goals / Non-Goals

**Goals:**
- Fire `KeyEvent.KEY_PRESSED` + `KeyEvent.KEY_RELEASED` for a given key string
- Support modifier combinations: `"Control+C"`, `"Shift+Tab"`, `"Control+Shift+Z"`
- Case-insensitive parsing; accept `Ctrl` as alias for `Control`, `Win`/`Meta` interchangeable
- Optional selector: if provided, fire on that node; if omitted, fire on the scene's focused node
- Add `press_key(key, **selector)` to Python client

**Non-Goals:**
- `KEY_TYPED` event (character input — that's `type()`)
- Holding keys down for duration
- OS-level key injection (Robot API) — in-JVM event dispatch is sufficient

## Decisions

### D1: Fire KEY_PRESSED + KEY_RELEASED (not KEY_TYPED)

Keyboard shortcuts respond to `KEY_PRESSED`. `KEY_TYPED` is for character input (already handled by `type()`). Always firing both PRESSED and RELEASED is the correct and complete simulation.

### D2: Key string format — Playwright style, case-insensitive

Format: `"[Modifier+]*Key"` where separator is `+`, parts are trimmed and uppercased before matching.

Modifier aliases (normalised to canonical before KeyCode lookup):
- `CTRL` → `CONTROL`
- `WIN` → `META`

Key part maps directly to `KeyCode.valueOf(name.toUpperCase())`, which covers all standard keys (`ENTER`, `ESCAPE`, `TAB`, `F1`–`F12`, `A`–`Z`, `DIGIT0`–`DIGIT9`, `UP`, `DOWN`, etc.).

### D3: Target node — selector optional

- If selector provided → resolve node, fire on that node
- If no selector → fire on scene's `focusOwner` (`scene.getFocusOwner()`)
- If focusOwner is null → fire on scene root

This means `press_key("Escape")` works as a global shortcut without needing to specify a node.

### D4: `press_key` belongs in `performOnFxThread()` (but selector is optional)

Unlike `close_app` (always top-level), `press_key` can optionally target a node. It goes in `performOnFxThread()` where node resolution happens, with special handling when selector is absent.

## Risks / Trade-offs

- **Unknown key name**: `KeyCode.valueOf()` throws `IllegalArgumentException` for unrecognised names. → Mitigation: catch and return `unknown_key` failure with the offending name.
- **No focused node**: If the app has no focus owner and no selector is given, fire on scene root — events will bubble normally.
- **Module access**: `KeyEvent` constructor is public; no extra `--add-opens` needed beyond what's already configured.
