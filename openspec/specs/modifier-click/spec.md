## Purpose

Define modifier-key support for the `click` action, allowing automation scripts to simulate Ctrl+click, Shift+click, and other modifier combinations on JavaFX nodes.

## Requirements

### Requirement: click supports modifier keys
The system SHALL accept an optional `modifiers` parameter on the `click` action.
When `modifiers` is provided and non-empty, the agent SHALL fire `MouseEvent.MOUSE_CLICKED`
with the corresponding modifier flags (`shiftDown`, `controlDown`, `altDown`, `metaDown`) set to `true`.
When `modifiers` is absent or empty, click behavior SHALL be identical to the current implementation.

#### Scenario: Ctrl+click on ListView item
- **WHEN** an automation script calls `click(id="serverList", modifiers=["Ctrl"])`
- **THEN** the agent fires `MOUSE_CLICKED` with `controlDown=true` on the target node and returns success

#### Scenario: Shift+click for range selection
- **WHEN** an automation script calls `click(id="serverList", modifiers=["Shift"])`
- **THEN** the agent fires `MOUSE_CLICKED` with `shiftDown=true` on the target node and returns success

#### Scenario: Combined Ctrl+Shift+click
- **WHEN** an automation script calls `click(id="serverList", modifiers=["Ctrl", "Shift"])`
- **THEN** the agent fires `MOUSE_CLICKED` with `controlDown=true` and `shiftDown=true` and returns success

#### Scenario: No modifiers preserves existing behavior
- **WHEN** an automation script calls `click(id="loginButton")` without `modifiers`
- **THEN** the agent fires the action using the existing `node.fire()` path and returns success

### Requirement: modifier strings are case-insensitive with aliases
The system SHALL parse modifier strings using the same rules as `press_key`:
case-insensitive matching, `Ctrl` accepted as alias for `Control`, `Win` accepted as alias for `Meta`.

#### Scenario: Ctrl alias accepted
- **WHEN** `click(id="serverList", modifiers=["ctrl"])` is called
- **THEN** the agent treats it identically to `modifiers=["Control"]` and sets `controlDown=true`

#### Scenario: Mixed case accepted
- **WHEN** `click(id="serverList", modifiers=["SHIFT"])` is called
- **THEN** the agent treats it identically to `modifiers=["Shift"]` and sets `shiftDown=true`

#### Scenario: Unknown modifier string returns failure
- **WHEN** `click(id="serverList", modifiers=["Bogus"])` is called
- **THEN** the agent returns a failure result with reason `unknown_modifier`

### Requirement: click Python API
The Python `click()` method SHALL accept an optional `modifiers: list[str] | None = None` keyword argument.
When provided, the modifiers list SHALL be included in the request payload as `"modifiers"`.
When absent or `None`, the payload SHALL NOT include a `"modifiers"` key.

#### Scenario: click with modifiers sends payload
- **WHEN** a Python script calls `client.click(id="serverList", modifiers=["Ctrl"])`
- **THEN** the HTTP request payload includes `{"modifiers": ["Ctrl"]}` in addition to the selector

#### Scenario: click without modifiers unchanged
- **WHEN** a Python script calls `client.click(id="loginButton")`
- **THEN** the HTTP request payload does NOT include a `"modifiers"` key
