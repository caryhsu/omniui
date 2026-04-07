## ADDED Requirements

### Requirement: Login app launches and serves health endpoint
The `login-app` JavaFX application SHALL start and expose the OmniUI agent health endpoint so that `OmniUI.launch()` can connect to it.

#### Scenario: App starts successfully
- **WHEN** `login-app` is launched with the OmniUI java agent on any available port
- **THEN** the agent health endpoint responds within 30 seconds

### Requirement: Login form nodes are accessible by ID
The login form SHALL expose the following node IDs for automation: `username` (TextField), `password` (PasswordField), `loginButton` (Button), `status` (Label).

#### Scenario: All required IDs are present
- **WHEN** the app has started
- **THEN** `get_text(id="status")` returns ok
- **THEN** `click(id="loginButton")` returns ok

### Requirement: Correct credentials show success status
The login form SHALL display "Success" in the `status` label when username is "admin" and password is "1234".

#### Scenario: Valid login
- **WHEN** `username` is set to "admin" and `password` to "1234" and `loginButton` is clicked
- **THEN** `verify_text(id="status", expected="Success")` returns ok

### Requirement: Incorrect credentials show failure status
The login form SHALL display a non-"Success" status when credentials are wrong.

#### Scenario: Invalid password
- **WHEN** `username` is set to "admin" and `password` to "wrong" and `loginButton` is clicked
- **THEN** `get_text(id="status")` returns a value that does NOT contain "Success"
