## Purpose

Define automation behavior for JavaFX Slider and Spinner controls.

## Requirements

### Requirement: Slider value read and write
The system SHALL support reading the current value of a `Slider` node and
setting it to a specified numeric value within the slider's min/max range.

#### Scenario: Read Slider value
- **WHEN** the automation client calls `get_value(id="<sliderId>")`
- **THEN** the system returns the current double value of the Slider

#### Scenario: Set Slider value within range
- **WHEN** the automation client calls `set_slider(id="<sliderId>", value=<number>)`
  and the value is within the Slider's min/max bounds
- **THEN** the Slider's value property is updated to the specified number

#### Scenario: Reject Slider value out of range
- **WHEN** the automation client calls `set_slider` with a value outside the
  Slider's min/max bounds
- **THEN** the system returns a failure result with reason `value_out_of_range`

### Requirement: Spinner value read, write and step
The system SHALL support reading the current value of a `Spinner`, setting it
to a specified value, and incrementing or decrementing by a given number of steps.

#### Scenario: Read Spinner value
- **WHEN** the automation client calls `get_value(id="<spinnerId>")`
- **THEN** the system returns the Spinner's current value as a string

#### Scenario: Set Spinner value
- **WHEN** the automation client calls `set_spinner(id="<spinnerId>", value="<val>")`
- **THEN** the Spinner's value factory is updated to the given value

#### Scenario: Increment Spinner
- **WHEN** the automation client calls `step_spinner(id="<spinnerId>", steps=<n>)`
  with a positive integer `n`
- **THEN** the Spinner increments by `n` steps

#### Scenario: Decrement Spinner
- **WHEN** the automation client calls `step_spinner(id="<spinnerId>", steps=<n>)`
  with a negative integer `n`
- **THEN** the Spinner decrements by `abs(n)` steps