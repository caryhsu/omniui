## ADDED Requirements

### Requirement: Python client connection API
The system SHALL provide a Python client entry point that establishes a session with the local OmniUI automation engine for a target JavaFX application.

#### Scenario: Connect from Python script
- **WHEN** a user calls `OmniUI.connect()` in a supported local environment
- **THEN** the client returns a session object capable of discovery, action, screenshot, OCR, and vision operations

### Requirement: High-level action API
The system SHALL provide high-level Python methods for `click`, `type`, `get_text`, and `verify_text` that accept selectors consistent with the selector resolution engine.

#### Scenario: Verify login status through Python client
- **WHEN** a script invokes `verify_text(id="status", expected="Success")`
- **THEN** the client resolves the target through the automation engine and fails the action if the retrieved text does not equal the expected value

### Requirement: Low-level troubleshooting API
The system SHALL provide low-level Python methods for `find`, `screenshot`, `ocr`, and `vision_match` to support debugging and advanced automation flows.

#### Scenario: Inspect OCR result explicitly
- **WHEN** a user calls `ocr(image)` through the Python client
- **THEN** the system returns recognized text results and positional metadata suitable for manual troubleshooting

### Requirement: Login-flow script compatibility
The system SHALL support execution of the Phase 1 demo login flow entirely from a Python automation script against a supported JavaFX application.

#### Scenario: Run complete login script
- **WHEN** a script clicks the username field, types credentials, clicks the login button, and verifies the success text
- **THEN** the full sequence executes successfully without requiring the user to switch APIs or tools mid-flow

### Requirement: Stable selector argument model
The system SHALL allow high-level Python actions to be invoked with selector fields such as `id`, `text`, and `type` without exposing backend-specific transport or adapter details in the script surface.

#### Scenario: Use structural selector arguments in click API
- **WHEN** a user calls `click(text="Login", type="Button")`
- **THEN** the Python API accepts the selector and delegates backend resolution without requiring explicit OCR or vision flags from the caller
