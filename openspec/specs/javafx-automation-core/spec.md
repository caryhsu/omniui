## Purpose

Define the core JavaFX automation behavior exposed by OmniUI through its Java agent integration.

## Requirements

### Requirement: Java agent connectivity
The system SHALL provide a Java agent that can be injected into a supported local JVM hosting a JavaFX application and expose node discovery and action execution endpoints to the Python automation engine without requiring target app source modifications.

#### Scenario: Connect Python client to injected JavaFX target
- **WHEN** a supported JavaFX application is launched with the OmniUI Java agent on the local machine
- **THEN** the Python client can establish a session with the local agent and use it to query nodes and invoke supported actions

#### Scenario: Fail clearly when app is not agent-enabled
- **WHEN** the JavaFX application is launched without OmniUI Java agent injection
- **THEN** the Python client cannot establish a session and receives a clear connection failure instead of silently binding to a built-in demo target

### Requirement: JavaFX node discovery
The system SHALL provide a JavaFX discovery capability that enumerates the active scene graph of the target application from agent-discovered runtime state and returns node records containing `fx:id` when present, node type, text content when present, and hierarchy path, including visible state for advanced controls such as popup-backed, hierarchical, and virtualized controls.

#### Scenario: Enumerate visible login form nodes from injected agent
- **WHEN** the Python client requests `get_nodes()` against an agent-enabled JavaFX application
- **THEN** the system returns node records discovered from the live JavaFX runtime without requiring the application to register its own stage with OmniUI

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
