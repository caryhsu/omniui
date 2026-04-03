## ADDED Requirements

### Requirement: JavaFX node discovery
The system SHALL provide a JavaFX discovery capability that enumerates the active scene graph of the target application and returns node records containing `fx:id` when present, node type, text content when present, and hierarchy path.

#### Scenario: Enumerate visible login form nodes
- **WHEN** the Python client requests `get_nodes()` against a connected JavaFX application
- **THEN** the system returns a collection of node records that includes visible login form controls with their available `fx:id`, node type, text content, and hierarchy path

### Requirement: Priority-based selector resolution
The system SHALL resolve selectors for JavaFX actions in the following order: exact `fx:id`, structural match using node type and text, OCR text match, and vision template match.

#### Scenario: Resolve button by fx:id before any fallback
- **WHEN** a script calls `click(id="loginButton")` and a JavaFX node exists with `fx:id="loginButton"`
- **THEN** the system resolves the target through the JavaFX adapter without invoking OCR or vision matching

#### Scenario: Resolve by type and text when fx:id is absent
- **WHEN** a script calls `click(text="Login", type="Button")` and no matching `fx:id` selector is provided
- **THEN** the system resolves the target to the JavaFX button whose type and visible text match the selector

### Requirement: Direct JavaFX interaction
The system SHALL execute supported actions against JavaFX-resolved nodes through runtime event dispatch or equivalent node-level interaction, not through screen coordinates.

#### Scenario: Click resolved JavaFX button without mouse coordinates
- **WHEN** a JavaFX node has been resolved for a click action
- **THEN** the system triggers the click through the JavaFX runtime on the application thread without requiring OS-level pointer movement

### Requirement: JavaFX action surface
The system SHALL support `click`, `type`, `get_text`, and `verify_text` operations for JavaFX-resolved elements through the Python API.

#### Scenario: Execute login flow through JavaFX selectors
- **WHEN** a script issues click, type, and verify operations against JavaFX-resolved username, password, login button, and status elements
- **THEN** the system completes the flow and reports the expected status text without requiring fallback resolution

### Requirement: Java agent connectivity
The system SHALL provide a Java agent that can attach to a supported local JVM hosting a JavaFX application and expose node discovery and action execution endpoints to the Python automation engine.

#### Scenario: Connect Python client to JavaFX target through local agent
- **WHEN** the target JavaFX application is running on the local machine and supports the Phase 1 attach model
- **THEN** the Python client can establish a session with the local Java agent and use it to query nodes and invoke supported actions

### Requirement: Action observability
The system SHALL report action diagnostics for every executed operation, including the requested selector, resolution tier, fallback tier used, and confidence score for OCR or vision paths when applicable.

#### Scenario: Log structural action diagnostics
- **WHEN** a script executes a click that resolves through JavaFX structure
- **THEN** the action result includes the original selector, resolution tier `javafx`, and indicates that no OCR or vision confidence score was required
