## ADDED Requirements

### Requirement: Python client selector model supports index field
The system SHALL document `index=N` as a valid selector field accepted by all `OmniUIClient` action methods. No Python code change is required — `index` passes through `**selector` naturally.

#### Scenario: Use index= to target second matching node
- **WHEN** an automation script calls `client.click(type="Button", index=1)`
- **THEN** the Python client passes `{"type": "Button", "index": 1}` as the selector to the Java agent, which resolves the second Button
