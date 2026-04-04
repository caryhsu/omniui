## Why

JavaFX nodes support a `Tooltip` that shows on hover — commonly used to display hints, validation errors, or status messages. OmniUI has no way to read tooltip text, making it impossible to assert that a tooltip says the right thing (e.g., "Invalid ID number format") without manually hovering. Adding `get_tooltip()` fills this gap with a single reflection call.

## What Changes

- Java agent: new `case "get_tooltip"` action — reads `node.getTooltip().getText()` via reflection
- Python client: new `get_tooltip(**selector) -> ActionResult` method
- Demo + documentation

## Capabilities

### New Capabilities
- `tooltip-verification`: The system SHALL expose `get_tooltip(**selector)` to read the tooltip text of a JavaFX node

### Modified Capabilities
- `python-automation-client`: Add `get_tooltip` to the public method list

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — `performOnFxThread()`: add `case "get_tooltip"`
- `omniui/core/engine.py` — add `get_tooltip(**selector)`
- `demo/python/tooltip_demo.py` — smoke test
- `demo/python/run_all.py` — add demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
