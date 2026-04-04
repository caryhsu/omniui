## ADDED Requirements

### Requirement: Python client exposes close_app method
The system SHALL add `close_app()` to the `OmniUIClient` public API so automation scripts can trigger application shutdown without dropping to process management.

#### Scenario: Script closes app as last step
- **WHEN** an automation script calls `client.close_app()` at the end of a test session
- **THEN** the application shuts down gracefully and the method returns without raising an exception
