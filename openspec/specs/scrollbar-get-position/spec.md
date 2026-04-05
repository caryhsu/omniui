## ADDED Requirements

### Requirement: get_scroll_position returns current ScrollBar value
The system SHALL provide `get_scroll_position(id)` on `OmniUIClient` that returns the current value of the `ScrollBar` identified by `id`. The result SHALL include `value` (float), `min` (float), and `max` (float).

#### Scenario: Query position of a ScrollBar
- **WHEN** `client.get_scroll_position(id="myScrollBar")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ActionResult.value` is a dict with keys `value`, `min`, `max`
- **THEN** `value` is within `[min, max]`

#### Scenario: ScrollBar not found
- **WHEN** `id` does not match any node in the scene
- **THEN** `ActionResult.ok` is `False`
- **THEN** result contains `reason="selector_not_found"`

#### Scenario: Node is not a ScrollBar
- **WHEN** `id` matches a node that is not a `ScrollBar`
- **THEN** `ActionResult.ok` is `False`
- **THEN** result contains `reason="action_not_supported"`
