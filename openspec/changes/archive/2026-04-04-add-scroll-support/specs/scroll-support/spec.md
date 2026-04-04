## ADDED Requirements

### Requirement: scroll_to brings target node into view
The system SHALL provide a `scroll_to` action that finds the nearest `ScrollPane` ancestor of the resolved target node and adjusts its `vvalue` so the node is visible within the viewport.

#### Scenario: Scroll down to an off-screen node
- **WHEN** an automation script calls `client.scroll_to(id="row50")` and the node is below the visible viewport
- **THEN** the Java agent walks the parent chain to find the enclosing `ScrollPane`, computes the proportional `vvalue`, and sets it so the node becomes visible

#### Scenario: Node already visible — no scroll needed
- **WHEN** an automation script calls `client.scroll_to(id="row1")` and the node is already within the viewport
- **THEN** the `vvalue` is clamped and no visible change occurs; the method returns `ok=True`

### Requirement: scroll_by adjusts scroll position by relative offset
The system SHALL provide a `scroll_by` action that accepts `delta_x` and `delta_y` as float offsets in the 0.0–1.0 scale and applies them to the `hvalue`/`vvalue` of the resolved `ScrollPane` (or the first `ScrollPane` in the scene if no selector is given).

#### Scenario: Scroll down by 20%
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.2, id="myScrollPane")`
- **THEN** the Java agent increases the `ScrollPane`'s `vvalue` by 0.2, clamped to 1.0

#### Scenario: Scroll up by 10%
- **WHEN** an automation script calls `client.scroll_by(0.0, -0.1, id="myScrollPane")`
- **THEN** the Java agent decreases `vvalue` by 0.1, clamped to 0.0

#### Scenario: scroll_by with no selector targets first ScrollPane
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.5)` with no selector
- **THEN** the Java agent finds the first `ScrollPane` in the scene and scrolls it
