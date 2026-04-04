## ADDED Requirements

### Requirement: Python client exposes press_key method
The system SHALL add `press_key(key, **selector)` to the `OmniUIClient` public API so automation scripts can trigger keyboard shortcuts and special key presses without dropping to process management or direct event construction.

#### Scenario: Press global shortcut
- **WHEN** an automation script calls `client.press_key("Escape")`
- **THEN** the Python client sends a `press_key` action with `key="Escape"` to the Java agent

#### Scenario: Press key on specific node
- **WHEN** an automation script calls `client.press_key("Enter", id="confirmBtn")`
- **THEN** the Python client sends a `press_key` action with `key="Enter"` and selector `id="confirmBtn"` to the Java agent

#### Scenario: Press modifier combination
- **WHEN** an automation script calls `client.press_key("Control+Z")`
- **THEN** the Python client sends a `press_key` action with `key="Control+Z"` to the Java agent
