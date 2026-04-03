## Purpose

Defines automation behavior for JavaFX Accordion and TitledPane controls, supporting expand, collapse, and state-read operations.

## Requirements

### Requirement: TitledPane expand and collapse
The system SHALL support expanding and collapsing a `TitledPane` node by setting its `expanded` property to `true` or `false` respectively.

#### Scenario: Expand a collapsed TitledPane
- **WHEN** the automation client calls `expand_pane(id="<titledPaneId>")` on a collapsed TitledPane
- **THEN** the TitledPane's `expanded` property becomes `true` and its content area becomes visible

#### Scenario: Collapse an expanded TitledPane
- **WHEN** the automation client calls `collapse_pane(id="<titledPaneId>")` on an expanded TitledPane
- **THEN** the TitledPane's `expanded` property becomes `false` and its content area is hidden

### Requirement: TitledPane expanded state read
The system SHALL support reading the current expanded state of a `TitledPane` node.

#### Scenario: Read expanded TitledPane state
- **WHEN** the automation client calls `get_expanded(id="<titledPaneId>")` on an expanded TitledPane
- **THEN** the system returns `true`

#### Scenario: Read collapsed TitledPane state
- **WHEN** the automation client calls `get_expanded(id="<titledPaneId>")` on a collapsed TitledPane
- **THEN** the system returns `false`

### Requirement: Accordion mutual exclusion
The system SHALL respect the Accordion's single-pane-expanded behavior: when one TitledPane inside an Accordion is expanded, all other TitledPanes in that Accordion are automatically collapsed by JavaFX.

#### Scenario: Expanding one pane collapses others in Accordion
- **WHEN** the automation client calls `expand_pane` on a TitledPane inside an Accordion
- **THEN** that TitledPane becomes expanded and all other TitledPanes in the same Accordion become collapsed automatically
