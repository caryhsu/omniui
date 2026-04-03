## ADDED Requirements

### Requirement: CheckBox selected state read
The system SHALL support reading the current checked state of a `CheckBox` node, returning `true` if checked and `false` if unchecked.

#### Scenario: Read a checked CheckBox
- **WHEN** the automation client calls `get_selected(id="<checkBoxId>")` on a CheckBox that is checked
- **THEN** the system returns `true`

#### Scenario: Read an unchecked CheckBox
- **WHEN** the automation client calls `get_selected(id="<checkBoxId>")` on a CheckBox that is unchecked
- **THEN** the system returns `false`

### Requirement: CheckBox selected state write
The system SHALL support setting the checked state of a `CheckBox` node to a specified boolean value.

#### Scenario: Check a CheckBox
- **WHEN** the automation client calls `set_selected(id="<checkBoxId>", value=True)`
- **THEN** the CheckBox becomes checked (`isSelected()` returns `true`)

#### Scenario: Uncheck a CheckBox
- **WHEN** the automation client calls `set_selected(id="<checkBoxId>", value=False)`
- **THEN** the CheckBox becomes unchecked (`isSelected()` returns `false`)

#### Scenario: CheckBoxes are independent
- **WHEN** the automation client sets multiple CheckBoxes to different states
- **THEN** each CheckBox retains its own state independently (CheckBoxes are not mutually exclusive unlike RadioButtons in a ToggleGroup)
