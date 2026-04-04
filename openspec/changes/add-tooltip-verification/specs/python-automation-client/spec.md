## ADDED Requirements

### Requirement: Python client exposes get_tooltip method
The system SHALL add `get_tooltip(**selector) -> ActionResult` to the `OmniUIClient` public API so callers can read tooltip text without dropping to the Java agent transport level.

#### Scenario: Read tooltip from Python
- **WHEN** an automation script calls `client.get_tooltip(id="myButton")`
- **THEN** the Python client dispatches the `get_tooltip` action with the selector and returns an `ActionResult` whose `value` is the tooltip text string
