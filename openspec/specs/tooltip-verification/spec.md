## Purpose

Define the `get_tooltip` action that reads the tooltip text attached to a JavaFX node.

## Requirements

### Requirement: get_tooltip reads the tooltip text of a node
The system SHALL provide a `get_tooltip(**selector)` action that resolves a node and returns the text of its attached `Tooltip`. If the node has no tooltip, the system SHALL return an empty string. If the node cannot be resolved, the system SHALL return a `selector_not_found` failure.

#### Scenario: Node has a tooltip
- **WHEN** an automation script calls `get_tooltip(id="submitButton")` and that node has a `Tooltip` with text `"Click to submit the form"`
- **THEN** the action returns `ok=True` with `value = "Click to submit the form"`

#### Scenario: Node has no tooltip
- **WHEN** an automation script calls `get_tooltip(id="usernameField")` and that node has no tooltip attached
- **THEN** the action returns `ok=True` with `value = ""`

#### Scenario: Node not found
- **WHEN** an automation script calls `get_tooltip(id="nonexistent")`
- **THEN** the action returns `ok=False` with reason `selector_not_found`

#### Scenario: Tooltip text is empty string
- **WHEN** a node has a `Tooltip` attached but `Tooltip.getText()` returns `null` or `""`
- **THEN** the action returns `ok=True` with `value = ""`

### Requirement: get_tooltip works with all existing selector combinations
The system SHALL accept `get_tooltip` with any selector field combination (`id`, `text`, `type`, `index`), consistent with all other getter actions.

#### Scenario: Use type= selector with get_tooltip
- **WHEN** an automation script calls `get_tooltip(type="Button", index=0)`
- **THEN** the action resolves the first Button node and returns its tooltip text
