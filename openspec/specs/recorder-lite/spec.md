## Purpose

Define the recorder-lite capability for capturing user interactions and generating stable automation scripts.

## Requirements

### Requirement: Recorder-lite click capture
The system SHALL support an optional recorder-lite mode that captures user click actions during a recording session on a supported local machine.

#### Scenario: Capture click during recording session
- **WHEN** recorder-lite is enabled and the user performs a click on the target application
- **THEN** the system records the click event for selector mapping and script generation

### Requirement: Selector-based script generation
The recorder-lite SHALL generate script output using stable selectors when the clicked target can be mapped to a JavaFX node or another supported selector source.

#### Scenario: Emit click script from JavaFX node mapping
- **WHEN** a recorded click maps to a JavaFX node with `fx:id="loginButton"`
- **THEN** the recorder outputs a script line equivalent to `click(id="loginButton")`

### Requirement: No coordinate-only script emission
The recorder-lite SHALL NOT emit script lines that rely only on raw screen coordinates when no stable selector can be derived for the recorded click.

#### Scenario: Suppress unstable recording output
- **WHEN** a recorded click cannot be mapped to a JavaFX selector, OCR text region, or another supported stable selector form
- **THEN** the recorder omits script generation for that click rather than emitting a coordinate-based action

### Requirement: Phase 1 recorder scope limit
The recorder-lite SHALL be limited to simple click-oriented script generation in Phase 1 and SHALL NOT claim support for full workflow recording coverage.

#### Scenario: Ignore unsupported interaction types
- **WHEN** a recording session observes an unsupported interaction such as drag-and-drop or complex keyboard choreography
- **THEN** the recorder excludes that interaction from generated script output and reports it as unsupported
