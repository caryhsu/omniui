## ADDED Requirements

### Requirement: hover action dispatches MOUSE_ENTERED and MOUSE_MOVED
The system SHALL provide a `hover` action that fires `MouseEvent.MOUSE_ENTERED` followed by
`MouseEvent.MOUSE_MOVED` on the target JavaFX node. This enables triggering tooltip display
and hover-state CSS pseudo-class changes.

#### Scenario: Hover on a node with tooltip
- **WHEN** an automation script calls `hover(id="demoButton")`
- **THEN** the agent fires `MOUSE_ENTERED` then `MOUSE_MOVED` on the node and returns success

#### Scenario: Selector not found
- **WHEN** an automation script calls `hover(id="nonExistent")`
- **THEN** the agent returns a failure result with reason `selector_not_found`

#### Scenario: Event dispatch fails
- **WHEN** the node fails to receive the synthesized event
- **THEN** the agent returns a failure result with reason `hover_failed` and an error message

### Requirement: hover uses node center as event coordinates
The system SHALL compute the center of the target node's local bounds as the event coordinates
and convert to screen coordinates via `node.localToScreen()`. If screen conversion is unavailable,
the agent SHALL fall back to (0, 0) for screen coordinates and still dispatch the event.

#### Scenario: Node with valid bounds
- **WHEN** the target node is visible and attached to a scene
- **THEN** events are dispatched with coordinates at the node's center

#### Scenario: Headless or off-screen node
- **WHEN** screen coordinates are unavailable
- **THEN** events use screen coordinates (0, 0) but are still dispatched

### Requirement: hover Python API
The Python `hover()` method SHALL accept selector keyword arguments (id, text, type, index)
and fire the hover action on the matching node.

#### Scenario: hover by id
- **WHEN** a Python script calls `client.hover(id="tooltipTarget")`
- **THEN** the HTTP request is sent with action `"hover"` and selector `{"id": "tooltipTarget"}`
