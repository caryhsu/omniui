## Why

OmniUI currently automates single-line text input via `TextField`, but lacks support
for `TextArea` (multi-line), `PasswordField` (masked input), and `Hyperlink` (clickable
link). These controls appear frequently in real-world JavaFX applications and are needed
to reach meaningful end-to-end automation coverage.

## What Changes

- Add `get_text` / `set_text` for `TextArea` (multi-line text node)
- Add `get_text` / `set_text` for `PasswordField` (masked; reads plain text via property)
- Add `click` for `Hyperlink` (fires the link action)
- Extend `LoginDemoApp` with TextArea, PasswordField, and Hyperlink demo sections
- Add Python demo scripts: `text_area_demo.py`, `password_field_demo.py`, `hyperlink_demo.py`

## Capabilities

### New Capabilities

- `text-area-automation`: Read and write multi-line text in `TextArea` nodes
- `password-field-automation`: Read and write text in `PasswordField` nodes
- `hyperlink-automation`: Click `Hyperlink` nodes and verify visited state

### Modified Capabilities

- `javafx-automation-core`: Extend supported node types for `get_text` / `set_text` actions to cover `TextArea` and `PasswordField`; add `Hyperlink` click support
- `advanced-javafx-demo-scenarios`: Add TextArea, PasswordField, and Hyperlink demo sections and scripts

## Impact

- `ReflectiveJavaFxTarget.java` — existing `handleType` already uses `setText`; `TextArea` and `PasswordField` both inherit from `TextInputControl` so no new handler needed; verify `getText` works via existing `get_text` path
- `LoginDemoApp.java` — new UI sections for the three controls
- `omniui/core/engine.py` — no new methods needed; `type()`, `get_text()`, and `click()` already exist
- `demo/python/` — three new demo scripts
