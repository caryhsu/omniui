## ADDED Requirements

### Requirement: set_scroll_position sets ScrollBar value
The system SHALL provide `set_scroll_position(id, value)` on `OmniUIClient` that sets the `ScrollBar` value to the given float. Values outside `[min, max]` SHALL be silently clamped.

#### Scenario: Set scroll position within range
- **WHEN** `client.set_scroll_position(id="myScrollBar", value=0.5)` is called with a value within `[min, max]`
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ScrollBar.getValue()` returns `0.5`

#### Scenario: Set scroll position out of range (clamped)
- **WHEN** `client.set_scroll_position(id="myScrollBar", value=999.0)` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ScrollBar.getValue()` equals `ScrollBar.getMax()`

#### Scenario: ScrollBar not found
- **WHEN** `id` does not match any node in the scene
- **THEN** `ActionResult.ok` is `False`
- **THEN** result contains `reason="selector_not_found"`
