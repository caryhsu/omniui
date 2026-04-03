## Purpose

Define automation behavior for JavaFX RadioButton and ToggleButton controls.

## Requirements

### Requirement: RadioButton selection
The system SHALL support reading the selected state of a `RadioButton` node
and setting it to selected via automation action.

#### Scenario: Read RadioButton selected state
- **WHEN** the automation client calls `get_selected(id="<radioButtonId>")`
- **THEN** the system returns `true` if the RadioButton is selected, `false` otherwise

#### Scenario: Select a RadioButton
- **WHEN** the automation client calls `set_selected(id="<radioButtonId>", value=True)`
- **THEN** the RadioButton becomes selected and any peer RadioButtons in the same
  ToggleGroup are automatically deselected by JavaFX

### Requirement: ToggleButton selection
The system SHALL support reading and toggling the selected state of a `ToggleButton`.

#### Scenario: Read ToggleButton selected state
- **WHEN** the automation client calls `get_selected(id="<toggleButtonId>")`
- **THEN** the system returns the current boolean selected state of the ToggleButton

#### Scenario: Toggle ToggleButton state
- **WHEN** the automation client calls `set_selected(id="<toggleButtonId>", value=True)`
- **THEN** the ToggleButton selected property becomes `true`

#### Scenario: Set ToggleButton to deselected
- **WHEN** the automation client calls `set_selected(id="<toggleButtonId>", value=False)`
- **THEN** the ToggleButton selected property becomes `false`