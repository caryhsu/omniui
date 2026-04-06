## MODIFIED Requirements

### Requirement: Core app login section removed
The `core-app` SHALL no longer contain a login form. The `username`, `password`, `loginButton`, and `status` node IDs SHALL NOT be present in `core-app` after this change.

#### Scenario: Login nodes absent from core-app
- **WHEN** `core-app` is running
- **THEN** `click(id="loginButton")` returns `ok=False` with reason `selector_not_found`
