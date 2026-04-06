## ADDED Requirements

### Requirement: Java agent handles ColorPicker commands
The Java agent SHALL handle `set_color`, `get_color`, `open_colorpicker`, and `dismiss_colorpicker` commands in the top-level `perform()` switch and the `performOnFxThread()` switch of `ReflectiveJavaFxTarget`.

#### Scenario: set_color routes to doSetColor
- **WHEN** the agent receives a `set_color` command
- **THEN** the action is dispatched to `doSetColor(selector, payload)` on the FX thread

#### Scenario: open_colorpicker uses performWithOverlayWait
- **WHEN** the agent receives an `open_colorpicker` command
- **THEN** the action uses `performWithOverlayWait` with an `isColorPickerWindow` predicate consistent with other overlay-based controls
