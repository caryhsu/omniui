## ADDED Requirements

### Requirement: Java agent captures user interaction events via Scene EventFilter

The system SHALL attach an `EventFilter` to the JavaFX `Scene` root when recording is started, intercepting `MOUSE_CLICKED` and `KEY_TYPED` events and buffering them as structured JSON objects. The buffer SHALL be capped at 1 000 events.

#### Scenario: Click event is captured
- **WHEN** recording is active and the user clicks a node with `fx:id="loginBtn"`
- **THEN** the buffer contains an entry with `type="click"`, `fxId="loginBtn"`, and a positive `timestamp`

#### Scenario: Type event is captured and aggregated
- **WHEN** recording is active and the user types "alice" into a TextField with `fx:id="username"`
- **THEN** the buffer contains a single entry with `type="type"`, `fxId="username"`, `text="alice"`

#### Scenario: Buffer is flushed and cleared via HTTP endpoint
- **WHEN** `GET /events?flush=true` is called
- **THEN** the response contains all buffered events as a JSON array
- **THEN** the buffer is empty after the response

#### Scenario: EventFilter is deregistered on stop
- **WHEN** `DELETE /events` is called
- **THEN** the EventFilter is removed from the Scene
- **THEN** subsequent user interactions are not buffered
