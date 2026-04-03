## ADDED Requirements

### Requirement: Standard JVM agent startup
The system SHALL support startup-time Java agent injection for JavaFX automation through a standard JVM `-javaagent` launch path.

#### Scenario: Launch target app with Java agent enabled
- **WHEN** a supported JavaFX application is started with the OmniUI Java agent using `-javaagent`
- **THEN** the Java agent starts inside the target JVM without requiring OmniUI classes to be referenced by the target application source

### Requirement: Agent-owned control server
The system SHALL start and own the OmniUI HTTP control server from inside the Java agent, not from target application bootstrap code.

#### Scenario: Agent starts control plane after injection
- **WHEN** the target JVM starts with the OmniUI Java agent enabled
- **THEN** the Java agent opens the loopback control endpoint and reports agent readiness without requiring the target application to call OmniUI startup APIs

### Requirement: Agent-driven JavaFX runtime discovery
The system SHALL discover JavaFX runtime state from the injected Java agent and register automation targets without requiring application code to call `registerStage`, `registerScene`, or equivalent OmniUI bridge APIs.

#### Scenario: Discover live JavaFX scene without app-side bridge call
- **WHEN** an injected Java agent is running inside a supported JavaFX application
- **THEN** the agent discovers a live JavaFX scene or stage and makes it available for node discovery and action execution

### Requirement: Plain launch isolation
The system SHALL allow the same JavaFX application to run without OmniUI control when it is launched without Java agent injection.

#### Scenario: Launch app in plain mode
- **WHEN** a supported JavaFX application is started without the OmniUI Java agent
- **THEN** the application runs normally and does not expose the OmniUI HTTP control endpoint
