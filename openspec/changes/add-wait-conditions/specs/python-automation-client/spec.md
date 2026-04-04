## ADDED Requirements

### Requirement: Python client exposes wait condition methods
The system SHALL add `wait_for_text`, `wait_for_visible`, `wait_for_enabled`, `wait_for_node`, and `wait_for_value` to the `OmniUIClient` public API so callers can await UI state transitions without using `time.sleep`.

#### Scenario: Script waits for async result without sleep
- **WHEN** an automation script calls `wait_for_text(id="result", expected="OK")` after triggering an async operation
- **THEN** the script blocks until the text matches or raises `TimeoutError`, without requiring a hardcoded `time.sleep` delay
