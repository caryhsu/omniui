## ADDED Requirements

### Requirement: Script generator converts recorded events to a Python test script string

The system SHALL provide a `generate_script(events: list[RecordedEvent]) -> str` function that returns a valid Python script string containing one `client.<action>(**selector)` call per event. Fragile selectors SHALL be annotated with an inline `# WARN: fragile selector` comment.

#### Scenario: Click event produces client.click call
- **WHEN** a `RecordedEvent` with `event_type="click"` and `fx_id="loginBtn"` is passed
- **THEN** the output contains `client.click(id="loginBtn")`

#### Scenario: Type event produces client.type call
- **WHEN** a `RecordedEvent` with `event_type="type"`, `fx_id="username"`, `text="alice"` is passed
- **THEN** the output contains `client.type(id="username", text="alice")`

#### Scenario: Fragile selector is annotated
- **WHEN** `infer_selector` returns a selector with `_fragile=True`
- **THEN** the generated line ends with `  # WARN: fragile selector`

#### Scenario: Generated script has standard header
- **WHEN** `generate_script` is called with any non-empty event list
- **THEN** the output starts with a comment header and `from omniui import OmniUI`
