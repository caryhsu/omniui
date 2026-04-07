# script-generation

## Purpose

Convert a list of recorded events into a valid Python test script string, with one client action call per event and inline warnings for fragile selectors.

## Requirements

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

### Requirement: assertion 事件產生 verify_* 呼叫

`generate_script` **必須** 將 `event_type="assertion"` 的事件轉換為對應的 Python 行。

#### Scenario: verify_text 斷言

- **WHEN** `RecordedEvent(event_type="assertion", fx_id="statusLabel", assertion_type="verify_text", expected="Success")`
- **THEN** the output contains `client.verify_text(id="statusLabel", expected="Success")`

#### Scenario: verify_visible 斷言

- **WHEN** `RecordedEvent(event_type="assertion", fx_id="submitBtn", assertion_type="verify_visible")`
- **THEN** the output contains `client.verify_visible(id="submitBtn")`

#### Scenario: verify_enabled 斷言

- **WHEN** `RecordedEvent(event_type="assertion", fx_id="submitBtn", assertion_type="verify_enabled")`
- **THEN** the output contains `client.verify_enabled(id="submitBtn")`

#### Scenario: 斷言行不帶 WARN 注解

- **WHEN** assertion 事件有穩定的 `fx_id`
- **THEN** the generated line does **not** contain `# WARN: fragile selector`

#### Scenario: assertion 步驟與 click 步驟混排順序正確

- **WHEN** the event sequence is `[click("loginBtn"), assertion("statusLabel", "verify_text", "Success")]`
- **THEN** the script output order is preserved:
  ```python
  client.click(id="loginBtn")
  client.verify_text(id="statusLabel", expected="Success")
  ```

