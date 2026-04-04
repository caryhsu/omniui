## ADDED Requirements

### Requirement: Python client exposes double_click method
The system SHALL add `double_click(**selector)` to the `OmniUIClient` public API so automation scripts can trigger double-click interactions on JavaFX nodes (such as expanding tree items or opening detail views) without manually constructing mouse events.

#### Scenario: Script double-clicks a tree item to expand it
- **WHEN** an automation script calls `client.double_click(id="myTreeItem")`
- **THEN** the Python client sends a `double_click` action to the Java agent and the tree item receives a `MouseEvent` with `clickCount=2`

#### Scenario: Script double-clicks a list item to open detail view
- **WHEN** an automation script calls `client.double_click(text="Record 1", type="ListCell")`
- **THEN** the Python client delegates to the agent which fires the synthesized double-click event on the matching node
