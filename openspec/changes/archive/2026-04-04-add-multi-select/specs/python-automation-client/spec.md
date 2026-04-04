## ADDED Requirements

### Requirement: Python client exposes select_multiple method
The system SHALL add `select_multiple(values: list[str], **selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can select multiple items in a ListView or TableView without constructing raw payloads.

#### Scenario: Call select_multiple with id selector
- **WHEN** an automation script calls `client.select_multiple(["Alpha", "Gamma"], id="serverList")`
- **THEN** the Python client dispatches a `select_multiple` action with payload `{"values": ["Alpha", "Gamma"]}` and selector `{"id": "serverList"}` to the Java agent

### Requirement: Python client exposes get_selected_items method
The system SHALL add `get_selected_items(**selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can read back all currently selected items from a multi-select control.

#### Scenario: Call get_selected_items
- **WHEN** an automation script calls `client.get_selected_items(id="serverList")`
- **THEN** the Python client dispatches a `get_selected_items` action and returns an `ActionResult` whose `value` is a list of selected item strings
