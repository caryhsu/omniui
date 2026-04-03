## MODIFIED Requirements

### Requirement: JavaFX node discovery
The system SHALL provide a JavaFX discovery capability that enumerates the active scene graph of the target application from agent-discovered runtime state and returns node records containing `fx:id` when present, node type, text content when present, and hierarchy path, including visible state for advanced controls such as popup-backed, hierarchical, and virtualized controls.

#### Scenario: Discover visible advanced-control state
- **WHEN** the Python client requests `get_nodes()` while advanced JavaFX demo scenarios are visible
- **THEN** the system returns node records for the currently materialized control state, including visible `ComboBox`, `ListView`, `TreeView`, and `TableView` structures

### Requirement: Advanced JavaFX interaction coverage
The system SHALL support automation of representative advanced JavaFX control interactions needed by the reference demos, including selection-oriented and hierarchy-oriented operations where required by the supported scenarios.

#### Scenario: Select visible list item in advanced demo
- **WHEN** a supported advanced demo scenario requires selecting a visible item in a selection-based JavaFX control
- **THEN** the system can perform the interaction through the JavaFX runtime and verify the resulting selected value or state

#### Scenario: Expand or select visible tree item in advanced demo
- **WHEN** a supported advanced demo scenario requires expanding or selecting a visible tree node
- **THEN** the system can drive the required JavaFX interaction and observe the resulting visible state
