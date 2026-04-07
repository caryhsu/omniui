# record-session

## Purpose

Provide a high-level recording session API on `OmniUIClient` that orchestrates event capture, selector inference, and script generation into a single `start_recording` / `stop_recording` workflow.

## Requirements

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

### Requirement: RecordedEvent 支援 assertion 事件類型

`RecordedEvent` **必須** 支援 `event_type="assertion"`，並包含下列額外欄位：

| 欄位 | 類型 | 說明 |
|------|------|------|
| `assertion_type` | `str` | `"verify_text"` / `"verify_visible"` / `"verify_enabled"` |
| `expected` | `str \| None` | verify_text 用；其他類型為 `None` |

#### Scenario: 建立 assertion 類型的 RecordedEvent

- **WHEN** `RecordedEvent(event_type="assertion", fx_id="statusLabel", assertion_type="verify_text", expected="Success")` is created
- **THEN** the object is valid, `event.assertion_type == "verify_text"`, `event.expected == "Success"`

#### Scenario: 非 assertion 事件不需要 assertion_type

- **WHEN** `RecordedEvent(event_type="click", fx_id="loginBtn")` is created
- **THEN** `assertion_type` defaults to `""`, existing behaviour is unaffected

