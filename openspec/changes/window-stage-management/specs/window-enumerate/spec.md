## ADDED Requirements

### Requirement: get_windows returns all open Stage titles
The system SHALL provide `get_windows()` on `OmniUIClient` that returns a list of title strings for all currently open JavaFX `Stage` windows.

#### Scenario: Single window open
- **WHEN** `client.get_windows()` is called with one Stage open
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ActionResult.value` is a list containing the title of that Stage

#### Scenario: Multiple windows open
- **WHEN** two or more Stages are open
- **THEN** `ActionResult.value` contains all their titles
