## ADDED Requirements

### Requirement: Agent exposes a non-destructive event polling endpoint
The agent SHALL expose `GET /sessions/{sessionId}/events/pending` that returns all
recorder events buffered since the last poll and advances an internal delivery cursor.
The endpoint SHALL NOT remove events from the main buffer — `DELETE /events` (stop/flush)
remains the only way to end the recording session.

#### Scenario: First poll returns all buffered events
- **WHEN** `GET /sessions/{sid}/events/pending` is called after three interactions
- **THEN** the response contains exactly those three events and `{"ok": true}`
- **THEN** the delivery cursor is advanced past those events

#### Scenario: Subsequent poll returns only new events
- **WHEN** a second poll is made after one new interaction
- **THEN** the response contains only the one new event (not the previously delivered ones)

#### Scenario: Poll during inactive session returns empty list
- **WHEN** `GET /sessions/{sid}/events/pending` is called when no recording is active
- **THEN** the response is `{"ok": true, "events": []}`

#### Scenario: Stop recording after polling flushes all events
- **WHEN** `DELETE /sessions/{sid}/events` is called after one or more polls
- **THEN** all events (including already-polled ones) are returned
- **THEN** the recording session is terminated and both buffer and cursor are reset

### Requirement: Python client exposes poll_events method
`OmniUI` SHALL expose `poll_events() -> list[dict]` that calls
`GET /sessions/{sessionId}/events/pending` and returns the raw event list.
The method SHALL only be valid while a recording session is active.

#### Scenario: poll_events returns incremental events
- **WHEN** `client.poll_events()` is called twice during an active recording
- **THEN** the second call returns only events that occurred after the first call

#### Scenario: poll_events outside recording session raises RuntimeError
- **WHEN** `client.poll_events()` is called without an active recording session
- **THEN** a `RuntimeError` is raised

### Requirement: Recorder GUI shows steps in real time during recording
The Recorder GUI SHALL start a background polling thread when `start_recording()` is
called. The thread SHALL call `poll_events()` every 500 ms and append any new steps
to the script preview widget. The thread SHALL be stopped before `stop_recording()` is
called; any events returned by `stop_recording` that were not yet displayed SHALL be
merged and shown in the final script.

#### Scenario: Steps appear within 500 ms of being recorded
- **WHEN** the user performs an interaction while the Recorder is recording
- **THEN** the corresponding script line appears in the preview within ~500 ms

#### Scenario: Final script matches stop_recording output
- **WHEN** the user presses Stop
- **THEN** the complete generated script (from `stop_recording`) replaces the
  incrementally-built preview to ensure consistency
