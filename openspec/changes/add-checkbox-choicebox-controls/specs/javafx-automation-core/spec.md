## ADDED Requirements

### Requirement: CheckBox and ChoiceBox control support
The system SHALL support `CheckBox` and `ChoiceBox` as first-class automation targets, discoverable via `get_nodes()` and actionable via the existing `get_selected`, `set_selected`, `select`, and `get_value` action paths.

#### Scenario: CheckBox node discoverable
- **WHEN** a scene contains a `CheckBox` node with an `fx:id`
- **THEN** `get_nodes()` returns a record for that node with the correct type and state

#### Scenario: ChoiceBox node discoverable
- **WHEN** a scene contains a `ChoiceBox` node with an `fx:id`
- **THEN** `get_nodes()` returns a record for that node with the correct type and current value
