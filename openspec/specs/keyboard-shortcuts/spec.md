## Purpose

Define the keyboard shortcut action behavior for the OmniUI Java agent: firing `KeyEvent.KEY_PRESSED` + `KeyEvent.KEY_RELEASED` on a target JavaFX node using a human-readable key string format.

## Requirements

### Requirement: press_key fires KEY_PRESSED and KEY_RELEASED on target node
The system SHALL provide a `press_key` action that fires `KeyEvent.KEY_PRESSED` followed by `KeyEvent.KEY_RELEASED` on the target JavaFX node. If no selector is given, the agent SHALL fire on the scene's current focus owner; if focus owner is null, fire on the scene root.

#### Scenario: Press single key globally (no selector)
- **WHEN** an automation script calls `press_key(key="Escape")`
- **THEN** the agent fires `KEY_PRESSED` + `KEY_RELEASED` for `KeyCode.ESCAPE` on the scene's focus owner and returns success

#### Scenario: Press key on specific node
- **WHEN** an automation script calls `press_key(key="Enter", id="confirmBtn")`
- **THEN** the agent fires `KEY_PRESSED` + `KEY_RELEASED` for `KeyCode.ENTER` on the node with id `confirmBtn`

#### Scenario: Unknown key name
- **WHEN** an automation script calls `press_key(key="Bogus")`
- **THEN** the agent returns a failure result with reason `unknown_key` and includes the offending key name

### Requirement: press_key supports modifier combinations
The system SHALL parse modifier+key combinations in the format `"[Modifier+]*Key"` (e.g., `"Control+C"`, `"Shift+Tab"`, `"Control+Shift+Z"`). Parsing SHALL be case-insensitive. The alias `Ctrl` SHALL be accepted as equivalent to `Control`; `Win` SHALL be accepted as equivalent to `Meta`.

#### Scenario: Fire Ctrl+C shortcut
- **WHEN** an automation script calls `press_key(key="Control+C")`
- **THEN** the agent fires `KEY_PRESSED` with `KeyCode.C`, `controlDown=true`, and returns success

#### Scenario: Case-insensitive modifier parsing
- **WHEN** an automation script calls `press_key(key="ctrl+c")`
- **THEN** the agent treats it identically to `"Control+C"` and fires the same event

#### Scenario: Ctrl alias accepted
- **WHEN** an automation script calls `press_key(key="CTRL+Z")`
- **THEN** the agent normalises `CTRL` to `CONTROL` and fires `KEY_PRESSED` with `KeyCode.Z`, `controlDown=true`

#### Scenario: Multi-modifier combination
- **WHEN** an automation script calls `press_key(key="Control+Shift+Z")`
- **THEN** the agent fires `KEY_PRESSED` with `KeyCode.Z`, `controlDown=true`, `shiftDown=true`

### Requirement: press_key falls back gracefully when scene has no focus owner
The system SHALL fire on the scene root when the scene's focus owner is null, rather than failing.

#### Scenario: No focus owner present
- **WHEN** `press_key` is called without a selector and the scene has no current focus owner
- **THEN** the event is dispatched to the scene root and the action returns success
