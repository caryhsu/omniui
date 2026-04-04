## ADDED Requirements

### Requirement: Python client exposes scroll_to method
The system SHALL add `scroll_to(**selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can bring a node into the visible area of its enclosing `ScrollPane` without manually computing scroll offsets.

#### Scenario: Call scroll_to with id selector
- **WHEN** an automation script calls `client.scroll_to(id="row50")`
- **THEN** the Python client dispatches a `scroll_to` action with selector `{"id": "row50"}` to the Java agent and returns an `ActionResult`

### Requirement: Python client exposes scroll_by method
The system SHALL add `scroll_by(delta_x: float, delta_y: float, **selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can scroll a `ScrollPane` by a relative offset.

#### Scenario: Call scroll_by with delta and selector
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.2, id="myScrollPane")`
- **THEN** the Python client dispatches a `scroll_by` action with payload `{"delta_x": 0.0, "delta_y": 0.2, "selector": {"id": "myScrollPane"}}` to the Java agent

#### Scenario: Call scroll_by without selector
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.5)` with no selector keyword arguments
- **THEN** the Python client dispatches the action with an empty selector, and the Java agent targets the first `ScrollPane` in the scene
