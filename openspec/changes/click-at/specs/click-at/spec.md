## ADDED Requirements

### Requirement: click_at fires a mouse click at scene coordinates
The system SHALL provide a `click_at(x, y)` method on `OmniUIClient` that sends a `click_at` action to the Java agent with the given x and y coordinates. The Java agent SHALL fire a `MouseEvent.MOUSE_CLICKED` (single click, PRIMARY button) at those scene-relative coordinates on the scene root.

#### Scenario: Click at valid coordinates
- **WHEN** `client.click_at(x=100, y=200)` is called
- **THEN** a single PRIMARY mouse click event is fired at scene position (100, 200)
- **THEN** the call returns an `ActionResult` with `ok=True`

#### Scenario: No scene available
- **WHEN** `click_at` is called but the scene has not been initialised
- **THEN** `ActionResult` with `ok=False` and `reason="no_scene"` is returned

### Requirement: click_at payload contains x and y
The `click_at` action payload sent to the Java agent SHALL contain integer or float fields `x` and `y` representing scene-relative coordinates.

#### Scenario: Payload structure
- **WHEN** `client.click_at(x=50, y=75)` is called
- **THEN** the HTTP action payload is `{"action": "click_at", "payload": {"x": 50, "y": 75}}`

### Requirement: click_at accepts keyword-only x and y arguments
`OmniUIClient.click_at` SHALL accept `x` and `y` as keyword arguments (e.g., `click_at(x=100, y=200)`) to prevent accidental argument order mistakes.

#### Scenario: Keyword argument call
- **WHEN** `client.click_at(x=10, y=20)` is called
- **THEN** the call succeeds and sends x=10, y=20 in the payload
