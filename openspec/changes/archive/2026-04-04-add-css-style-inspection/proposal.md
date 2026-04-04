## Why

JavaFX validation patterns often use CSS to communicate state: a red border on an invalid TextField, a green checkmark on a valid Label, an `"error"` CSS class toggled on/off. OmniUI can already read text and tooltip, but has no way to inspect inline style or CSS class — making it impossible to assert color-based or class-based validation feedback without reading label text. Adding `get_style()` and `get_style_class()` fills this gap with two simple reflection getters.

## What Changes

- Java agent: two new cases in `performOnFxThread()` — `get_style` and `get_style_class`
- Python client: `get_style(**selector)` and `get_style_class(**selector)` methods
- Demo + documentation

## Capabilities

### New Capabilities
- `css-style-inspection`: The system SHALL expose `get_style()` and `get_style_class()` to read a node's inline CSS style string and applied CSS class list

### Modified Capabilities
- `python-automation-client`: Add `get_style` and `get_style_class` to the public method list

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — `performOnFxThread()`: two new cases
- `omniui/core/engine.py` — two new methods
- `demo/python/css_style_demo.py` — smoke test (uses inline style set on a node)
- `demo/python/run_all.py` — add demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
