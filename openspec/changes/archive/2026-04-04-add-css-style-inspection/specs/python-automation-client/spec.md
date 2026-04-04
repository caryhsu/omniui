## ADDED Requirements

### Requirement: Python client exposes get_style and get_style_class methods
The system SHALL add `get_style(**selector) -> ActionResult` and `get_style_class(**selector) -> ActionResult` to the `OmniUIClient` public API.

#### Scenario: Read inline style from Python
- **WHEN** an automation script calls `client.get_style(id="validationLabel")`
- **THEN** the Python client returns an `ActionResult` whose `value` is the inline style string (e.g. `"-fx-text-fill: red;"`)

#### Scenario: Read CSS class list from Python
- **WHEN** an automation script calls `client.get_style_class(id="errorField")`
- **THEN** the Python client returns an `ActionResult` whose `value` is a list of CSS class name strings
