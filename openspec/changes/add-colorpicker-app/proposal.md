## Why

OmniUI previously implemented ColorPicker automation (`set_color`, `get_color`, `open_colorpicker`, `dismiss_colorpicker`) in the old `LoginDemoApp`, but the code was lost during the demo-app reorganization. The spec exists but the implementation does not, leaving ColorPicker as a documented-but-broken feature.

## What Changes

- Restore `set_color` / `get_color` / `open_colorpicker` / `dismiss_colorpicker` in Java agent (`ReflectiveJavaFxTarget.java`)
- Restore Python client methods in `engine.py` and `locator.py`
- Create standalone `color-app` (port 48106) with a `ColorPicker` + result label + reset button
- Create `demo/python/color/color_demo.py` automation script
- Integrate into `run_all.py`

## Capabilities

### New Capabilities
- `colorpicker-demo-scenarios`: color-app demo app UI layout and expected Python automation scenarios

### Modified Capabilities
- `colorpicker-automation`: restoring previously-lost Java agent + Python API implementation (spec already exists, no requirement changes)
- `javafx-automation-core`: adding `set_color` / `get_color` / `open_colorpicker` / `dismiss_colorpicker` to the agent command table

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java` — 4 new cases restored
- `omniui/core/engine.py` — 4 new methods
- `omniui/locator.py` — 4 new methods
- New `demo/java/color-app/` Maven module (port 48106)
- New `demo/python/color/` package
- `demo/python/run_all.py` updated
