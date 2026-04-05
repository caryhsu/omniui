## ADDED Requirements

### Requirement: OmniUIClient exposes start_recording / stop_recording session API

The system SHALL provide `start_recording()` and `stop_recording() -> RecordedScript` methods on `OmniUIClient`. `start_recording()` SHALL register the EventFilter on the agent. `stop_recording()` SHALL flush events, deregister the filter, run selector inference, generate the script, and return a `RecordedScript` object.

#### Scenario: start_recording registers the EventFilter
- **WHEN** `client.start_recording()` is called
- **THEN** a `POST /events/start` request is sent to the agent
- **THEN** subsequent user interactions in the app are buffered

#### Scenario: stop_recording returns a RecordedScript
- **WHEN** `client.stop_recording()` is called after interactions
- **THEN** a `DELETE /events` (flush) request is sent to the agent
- **THEN** a `RecordedScript` is returned with non-empty `events` and `script` fields

#### Scenario: RecordedScript.save writes the script to disk
- **WHEN** `script.save("test_login.py")` is called
- **THEN** a file at the given path contains the generated Python script string

#### Scenario: Calling stop_recording without start_recording raises an error
- **WHEN** `client.stop_recording()` is called without a prior `start_recording()`
- **THEN** a `RuntimeError` is raised with a descriptive message
