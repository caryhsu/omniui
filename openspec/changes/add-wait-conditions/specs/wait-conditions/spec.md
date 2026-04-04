## ADDED Requirements

### Requirement: wait_for_text polls until node text matches expected value
The system SHALL provide a `wait_for_text(id, expected, timeout=5.0, interval=0.2)` method that polls until the text of the node with the given `id` equals `expected`, raising `TimeoutError` if not matched within `timeout` seconds.

#### Scenario: Node text matches before timeout
- **WHEN** `wait_for_text(id="statusLabel", expected="Done")` is called and the node's text becomes `"Done"` within the timeout
- **THEN** the method returns without raising an exception

#### Scenario: Node text does not match within timeout
- **WHEN** `wait_for_text(id="statusLabel", expected="Done", timeout=2.0)` is called and the text never becomes `"Done"` within 2 seconds
- **THEN** the method raises `TimeoutError` with a descriptive message

### Requirement: wait_for_visible polls until node is visible
The system SHALL provide a `wait_for_visible(id, timeout=5.0, interval=0.2)` method that polls until `is_visible(id=id)` returns `True`, raising `TimeoutError` if not reached within `timeout` seconds.

#### Scenario: Node becomes visible before timeout
- **WHEN** `wait_for_visible(id="resultPanel")` is called and the node becomes visible within the timeout
- **THEN** the method returns without raising an exception

#### Scenario: Node never becomes visible within timeout
- **WHEN** `wait_for_visible(id="resultPanel", timeout=2.0)` is called and the node never becomes visible
- **THEN** the method raises `TimeoutError`

### Requirement: wait_for_enabled polls until node is enabled
The system SHALL provide a `wait_for_enabled(id, timeout=5.0, interval=0.2)` method that polls until `is_enabled(id=id)` returns `True`, raising `TimeoutError` if not reached within `timeout` seconds.

#### Scenario: Node becomes enabled before timeout
- **WHEN** `wait_for_enabled(id="submitButton")` is called and the node becomes enabled within the timeout
- **THEN** the method returns without raising an exception

#### Scenario: Node never becomes enabled within timeout
- **WHEN** `wait_for_enabled(id="submitButton", timeout=2.0)` is called and the node remains disabled
- **THEN** the method raises `TimeoutError`

### Requirement: wait_for_node polls until a node with given id appears
The system SHALL provide a `wait_for_node(id, timeout=5.0, interval=0.2)` method that polls `get_nodes()` until a node with matching `fxId` is found, raising `TimeoutError` if not found within `timeout` seconds.

#### Scenario: Node appears before timeout
- **WHEN** `wait_for_node(id="dynamicWidget")` is called and a node with `fxId="dynamicWidget"` appears in discovery within the timeout
- **THEN** the method returns without raising an exception

#### Scenario: Node never appears within timeout
- **WHEN** `wait_for_node(id="dynamicWidget", timeout=2.0)` is called and no such node appears
- **THEN** the method raises `TimeoutError`

### Requirement: wait_for_value is an alias for wait_for_text
The system SHALL provide a `wait_for_value(id, expected, timeout=5.0, interval=0.2)` method with identical behavior to `wait_for_text`.

#### Scenario: wait_for_value behaves like wait_for_text
- **WHEN** `wait_for_value(id="field", expected="42")` is called and the node's text becomes `"42"` within the timeout
- **THEN** the method returns without raising an exception
