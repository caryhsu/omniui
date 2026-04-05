## ADDED Requirements

### Requirement: snapshot returns a UISnapshot with all current nodes

The system SHALL provide `client.snapshot()` on `OmniUIClient` that returns a `UISnapshot` containing a `nodes` list (all current scene graph nodes) and a `timestamp` (float, seconds since epoch).

#### Scenario: Basic snapshot
- **WHEN** `client.snapshot()` is called
- **THEN** the result is a `UISnapshot` instance
- **THEN** `result.nodes` is a non-empty list of node dicts
- **THEN** `result.timestamp` is a positive float

#### Scenario: Snapshot captures node attributes
- **WHEN** `client.snapshot()` is called
- **THEN** each node dict contains at least `fxId`, `text`, `nodeType`, `enabled`, `visible` keys
