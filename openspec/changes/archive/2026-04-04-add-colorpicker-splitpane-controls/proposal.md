## Why

`ColorPicker` and `SplitPane` are standard JavaFX controls used in many desktop applications but are not yet covered by OmniUI automation. Adding them completes the commonly-used control set and enables tests that involve color selection workflows and resizable panel layouts.

## What Changes

- Add `set_color` and `get_color` actions for `ColorPicker` (direct value set/read, no popup required)
- Add `open_colorpicker` / `dismiss_colorpicker` actions for popup-based color picker interaction
- Add `get_divider_positions` and `set_divider_position` actions for `SplitPane`
- Add Python engine methods: `set_color`, `get_color`, `open_colorpicker`, `dismiss_colorpicker`, `get_divider_positions`, `set_divider_position`
- Add demo UI sections and demo scripts for both controls
- Add `run_all.py` entries for both new demos

## Capabilities

### New Capabilities

- `colorpicker-automation`: Automation actions for JavaFX `ColorPicker` — set/get color value and open/dismiss the color picker popup
- `splitpane-automation`: Automation actions for JavaFX `SplitPane` — read and set divider positions

### Modified Capabilities

- `javafx-automation-core`: Add `ColorPicker` and `SplitPane` action contracts to the core automation spec
- `advanced-javafx-demo-scenarios`: Add demo scenario specs for ColorPicker and SplitPane

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new action cases
- `omniui/core/engine.py` — new public methods
- `demo/javafx-login-app/src/main/java/dev/omniui/demo/LoginDemoApp.java` — new UI sections
- `demo/python/color_picker_demo.py` — new demo script
- `demo/python/split_pane_demo.py` — new demo script
- `demo/python/run_all.py` — add both new demos
- jlink image must be rebuilt after agent changes (`mvn install -f java-agent/pom.xml` + `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`)
