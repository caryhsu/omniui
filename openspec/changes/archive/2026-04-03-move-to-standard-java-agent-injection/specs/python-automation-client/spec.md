## MODIFIED Requirements

### Requirement: Python client connection API
The system SHALL provide a Python client entry point that establishes a session only when the target JavaFX application is running with OmniUI agent injection enabled.

#### Scenario: Connect from Python script to agent-enabled app
- **WHEN** a user calls `OmniUI.connect()` and the target JavaFX application is running with the OmniUI Java agent enabled
- **THEN** the client returns a session object capable of discovery, action, screenshot, OCR, and vision operations

#### Scenario: Fail connection to plain app mode
- **WHEN** a user calls `OmniUI.connect()` and the target JavaFX application is running without the OmniUI Java agent
- **THEN** the client fails the connection attempt explicitly instead of behaving as though a controllable target is present

### Requirement: Login-flow script compatibility
The system SHALL support execution of the Phase 1 demo login flow entirely from a Python automation script against the agent-enabled launch mode of a supported JavaFX application.

#### Scenario: Run complete login script against injected app
- **WHEN** the demo JavaFX application is started in agent-enabled mode and a script clicks the username field, types credentials, clicks the login button, and verifies the success text
- **THEN** the full sequence executes successfully without requiring the application source to contain OmniUI-specific bootstrap code
