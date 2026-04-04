### Requirement: Python client exposes Locator
The system SHALL provide a `Locator` class obtainable via `client.locator(**selector)` that stores a node selector and re-uses it on every subsequent call, removing selector repetition from automation scripts and Page Object Models.

#### Scenario: Obtain locator and call methods without repeating selector
- **WHEN** a script calls `loc = client.locator(id="loginBtn")` and then calls `loc.click()`, `loc.verify_text("Login")`, `loc.wait_for_visible()`
- **THEN** each call is dispatched to the underlying client with `id="loginBtn"` merged in automatically

#### Scenario: Locator raises ValueError when created with no selector
- **WHEN** a script calls `client.locator()` with no arguments
- **THEN** the Python client raises `ValueError` immediately

#### Scenario: Locator wait_for_* raises ValueError if no id= given
- **WHEN** a script creates a locator with `text=` only and calls `loc.wait_for_visible()`
- **THEN** the Python client raises `ValueError` with a message indicating `id=` is required
