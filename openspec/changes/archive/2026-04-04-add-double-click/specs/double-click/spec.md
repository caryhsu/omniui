## ADDED Requirements

### Requirement: Double-click action dispatches MouseEvent with clickCount=2
The system SHALL provide a `double_click` action that fires a synthesized `MouseEvent.MOUSE_CLICKED` event with `clickCount=2` on the target JavaFX node, enabling automation of interactions such as expanding tree nodes, opening detail views from list/table rows, and invoking custom double-click handlers.

#### Scenario: Double-click on a TreeView item
- **WHEN** an automation script calls `double_click(id="myTreeItem")`
- **THEN** the Java agent fires a `MouseEvent.MOUSE_CLICKED` event with `clickCount=2` on the matching node and returns success

#### Scenario: Double-click on a ListView item
- **WHEN** an automation script calls `double_click(text="Item 1", type="ListCell")`
- **THEN** the Java agent fires a `MouseEvent.MOUSE_CLICKED` event with `clickCount=2` on the matching node and returns success

#### Scenario: Selector not found
- **WHEN** an automation script calls `double_click(id="nonExistent")`
- **THEN** the Java agent returns a failure result with reason `selector_not_found`

#### Scenario: Node cannot accept mouse events
- **WHEN** an automation script calls `double_click` on a node that fails to receive the synthesized event
- **THEN** the Java agent returns a failure result with reason `double_click_failed` and an error message

### Requirement: Double-click uses node center as event coordinates
The system SHALL compute the center of the target node's local bounds as the event's (x, y) coordinates and convert them to screen coordinates using `node.localToScreen()`. If screen conversion is unavailable (headless or off-screen node), the agent SHALL fall back to (0, 0) for screen coordinates and still dispatch the event.

#### Scenario: Node with valid bounds and scene
- **WHEN** the target node is visible and attached to a scene
- **THEN** the event is created with (x, y) at the node's center and valid screen coordinates

#### Scenario: Node without screen position (headless/off-screen)
- **WHEN** the target node has no screen position available
- **THEN** the event is created with screen coordinates (0, 0) but still dispatched to the node
