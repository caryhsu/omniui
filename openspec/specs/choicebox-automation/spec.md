## Purpose

Define automation behavior for JavaFX ChoiceBox controls.

## Requirements

### Requirement: ChoiceBox item selection
The system SHALL support selecting an item in a `ChoiceBox` node by matching its string value.

#### Scenario: Select a ChoiceBox item by value
- **WHEN** the automation client calls `select(id="<choiceBoxId>", value="<item>")`
- **THEN** the ChoiceBox's selection model selects the matching item and it becomes the current value

#### Scenario: Report error when item not found
- **WHEN** the automation client calls `select(id="<choiceBoxId>", value="<nonExistentItem>")`
- **THEN** the system returns a failure result with reason `item_not_found`

### Requirement: ChoiceBox current value read
The system SHALL support reading the currently selected value of a `ChoiceBox` node.

#### Scenario: Read the current ChoiceBox value
- **WHEN** the automation client calls `get_value(id="<choiceBoxId>")` after an item has been selected
- **THEN** the system returns the currently selected item's string representation

#### Scenario: Read ChoiceBox value when nothing is selected
- **WHEN** the automation client calls `get_value(id="<choiceBoxId>")` and no item is selected
- **THEN** the system returns `null` or an empty string
