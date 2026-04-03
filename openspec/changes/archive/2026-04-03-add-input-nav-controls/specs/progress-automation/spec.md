## ADDED Requirements

### Requirement: ProgressBar and ProgressIndicator value read
The system SHALL support reading the current progress value of a `ProgressBar`
or `ProgressIndicator` node. The progress value is a double in the range 0.0–1.0,
or -1.0 to indicate an indeterminate state.

#### Scenario: Read ProgressBar value
- **WHEN** the automation client calls `get_progress(id="<progressBarId>")`
- **THEN** the system returns the current progress as a double (0.0–1.0)

#### Scenario: Read indeterminate progress
- **WHEN** the automation client calls `get_progress` on a ProgressBar or
  ProgressIndicator in indeterminate state
- **THEN** the system returns `-1.0`

#### Scenario: Read ProgressIndicator value
- **WHEN** the automation client calls `get_progress(id="<progressIndicatorId>")`
- **THEN** the system returns the current progress as a double (0.0–1.0)
