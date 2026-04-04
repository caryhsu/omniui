## Purpose

Define the stable Python client behavior for connecting to an agent-enabled JavaFX application and running automation flows.

## Requirements

### Requirement: Python client connection API
The system SHALL provide a Python client entry point that establishes a session only when the target JavaFX application is running with OmniUI agent injection enabled.

#### Scenario: Connect from Python script to agent-enabled app
- **WHEN** a user calls `OmniUI.connect()` and the target JavaFX application is running with the OmniUI Java agent enabled
- **THEN** the client returns a session object capable of discovery, action, screenshot, OCR, and vision operations

#### Scenario: Fail connection to plain app mode
- **WHEN** a user calls `OmniUI.connect()` and the target JavaFX application is running without the OmniUI Java agent
- **THEN** the client fails the connection attempt explicitly instead of behaving as though a controllable target is present

### Requirement: Login-flow script compatibility
The system SHALL support execution of the Phase 1 demo login flow entirely from a Python automation script against the agent-enabled launch mode of a supported JavaFX application.

#### Scenario: Run complete login script against injected app
- **WHEN** the demo JavaFX application is started in agent-enabled mode and a script clicks the username field, types credentials, clicks the login button, and verifies the success text
- **THEN** the full sequence executes successfully without requiring the application source to contain OmniUI-specific bootstrap code

### Requirement: Python client advanced-control compatibility
The system SHALL allow Python automation scripts to exercise the supported advanced JavaFX demo scenarios without dropping down to backend-specific transport details.

#### Scenario: Run advanced scenario from Python
- **WHEN** a user runs a Python script against a supported advanced JavaFX demo scenario
- **THEN** the script can express the needed interaction through the OmniUI client API and inspect the resulting state through normal client methods

### Requirement: High-level action API
The system SHALL provide high-level Python methods sufficient to express the supported advanced JavaFX demo interactions, extending beyond `click` and `type` if required by the supported scenario set.

#### Scenario: Use selection-oriented action for advanced control
- **WHEN** a supported advanced JavaFX scenario requires an interaction that is better modeled as selection or expansion instead of a raw click
- **THEN** the Python client exposes a high-level action that represents that interaction without requiring the user to manipulate transport payloads directly

### Requirement: Low-level troubleshooting API
The system SHALL provide low-level Python methods for `find`, `screenshot`, `ocr`, and `vision_match` to support debugging and advanced automation flows.

#### Scenario: Inspect OCR result explicitly
- **WHEN** a user calls `ocr(image)` through the Python client
- **THEN** the system returns recognized text results and positional metadata suitable for manual troubleshooting

### Requirement: Stable selector argument model
The system SHALL allow high-level Python actions to be invoked with selector fields such as `id`, `text`, `type`, and `index` without exposing backend-specific transport or adapter details in the script surface.

#### Scenario: Use structural selector arguments in click API
- **WHEN** a user calls `click(text="Login", type="Button")`
- **THEN** the Python API accepts the selector and delegates backend resolution without requiring explicit OCR or vision flags from the caller

#### Scenario: Use index= to target second matching node
- **WHEN** an automation script calls `client.click(type="Button", index=1)`
- **THEN** the Python client passes `{"type": "Button", "index": 1}` as the selector to the Java agent, which resolves the second Button

### Requirement: is_visible method
The Python client SHALL expose `is_visible(**selector) -> bool` as a public method, consistent in style with `is_visited()` and `get_expanded()`.

### Requirement: is_enabled method
The Python client SHALL expose `is_enabled(**selector) -> bool` as a public method, consistent in style with `is_visited()` and `get_expanded()`.

### Requirement: Python client exposes wait condition methods
The system SHALL add `wait_for_text`, `wait_for_visible`, `wait_for_enabled`, `wait_for_node`, and `wait_for_value` to the `OmniUIClient` public API so callers can await UI state transitions without using `time.sleep`.

#### Scenario: Script waits for async result without sleep
- **WHEN** an automation script calls `wait_for_text(id="result", expected="OK")` after triggering an async operation
- **THEN** the script blocks until the text matches or raises `TimeoutError`, without requiring a hardcoded `time.sleep` delay

### Requirement: Python client exposes close_app method
The system SHALL add `close_app()` to the `OmniUIClient` public API so automation scripts can trigger application shutdown without dropping to process management.

#### Scenario: Script closes app as last step
- **WHEN** an automation script calls `client.close_app()` at the end of a test session
- **THEN** the application shuts down gracefully and the method returns without raising an exception

### Requirement: Python client exposes double_click method
The system SHALL add `double_click(**selector)` to the `OmniUIClient` public API so automation scripts can trigger double-click interactions on JavaFX nodes (such as expanding tree items or opening detail views) without manually constructing mouse events.

#### Scenario: Script double-clicks a tree item to expand it
- **WHEN** an automation script calls `client.double_click(id="myTreeItem")`
- **THEN** the Python client sends a `double_click` action to the Java agent and the tree item receives a `MouseEvent` with `clickCount=2`

#### Scenario: Script double-clicks a list item to open detail view
- **WHEN** an automation script calls `client.double_click(text="Record 1", type="ListCell")`
- **THEN** the Python client delegates to the agent which fires the synthesized double-click event on the matching node

### Requirement: Python client exposes press_key method
The system SHALL add `press_key(key, **selector)` to the `OmniUIClient` public API so automation scripts can trigger keyboard shortcuts and special key presses without dropping to process management or direct event construction.

#### Scenario: Press global shortcut
- **WHEN** an automation script calls `client.press_key("Escape")`
- **THEN** the Python client sends a `press_key` action with `key="Escape"` to the Java agent

#### Scenario: Press key on specific node
- **WHEN** an automation script calls `client.press_key("Enter", id="confirmBtn")`
- **THEN** the Python client sends a `press_key` action with `key="Enter"` and selector `id="confirmBtn"` to the Java agent

#### Scenario: Press modifier combination
- **WHEN** an automation script calls `client.press_key("Control+Z")`
- **THEN** the Python client sends a `press_key` action with `key="Control+Z"` to the Java agent

### Requirement: Python client exposes get_tooltip method
The system SHALL add `get_tooltip(**selector) -> ActionResult` to the `OmniUIClient` public API so callers can read tooltip text without dropping to the Java agent transport level.

#### Scenario: Read tooltip from Python
- **WHEN** an automation script calls `client.get_tooltip(id="myButton")`
- **THEN** the Python client dispatches the `get_tooltip` action with the selector and returns an `ActionResult` whose `value` is the tooltip text string

### Requirement: Python client exposes get_style and get_style_class methods
The system SHALL add `get_style(**selector) -> ActionResult` and `get_style_class(**selector) -> ActionResult` to the `OmniUIClient` public API.

#### Scenario: Read inline style from Python
- **WHEN** an automation script calls `client.get_style(id="validationLabel")`
- **THEN** the Python client returns an `ActionResult` whose `value` is the inline style string (e.g. `"-fx-text-fill: red;"`)

#### Scenario: Read CSS class list from Python
- **WHEN** an automation script calls `client.get_style_class(id="errorField")`
- **THEN** the Python client returns an `ActionResult` whose `value` is a list of CSS class name strings

### Requirement: verify_text supports flexible match modes
The system SHALL accept an optional `match` parameter in `verify_text(expected, *, match="exact", **selector)` supporting the following modes:

- `"exact"` (default) — `actual == expected`; preserves all existing behaviour
- `"contains"` — `expected in actual`
- `"starts_with"` — `actual.startswith(expected)`
- `"regex"` — `re.search(expected, actual) is not None`

An unknown `match` value SHALL raise `ValueError`.

#### Scenario: Default exact match unchanged
- **WHEN** a script calls `client.verify_text("Success", id="status")`
- **THEN** the result behaves identically to before — `ok=True` only when the text equals `"Success"` exactly

#### Scenario: contains match
- **WHEN** a script calls `client.verify_text("Success", match="contains", id="status")` and the actual text is `"Login Success (2026-04-04)"`
- **THEN** `result.ok = True` because `"Success"` is found within the actual text

#### Scenario: starts_with match
- **WHEN** a script calls `client.verify_text("Error:", match="starts_with", id="errorLabel")` and the actual text is `"Error: invalid input"`
- **THEN** `result.ok = True`

#### Scenario: regex match
- **WHEN** a script calls `client.verify_text(r"\d+ items", match="regex", id="countLabel")` and the actual text is `"42 items"`
- **THEN** `result.ok = True` because `re.search(r"\d+ items", "42 items")` matches

#### Scenario: unknown match mode raises ValueError
- **WHEN** a script calls `client.verify_text("x", match="fuzzy", id="label")`
- **THEN** the Python client raises `ValueError` immediately

### Requirement: Python client exposes scroll_to method
The system SHALL add `scroll_to(**selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can bring a node into the visible area of its enclosing `ScrollPane` without manually computing scroll offsets.

#### Scenario: Call scroll_to with id selector
- **WHEN** an automation script calls `client.scroll_to(id="row50")`
- **THEN** the Python client dispatches a `scroll_to` action with selector `{"id": "row50"}` to the Java agent and returns an `ActionResult`

### Requirement: Python client exposes scroll_by method
The system SHALL add `scroll_by(delta_x: float, delta_y: float, **selector) -> ActionResult` to the `OmniUIClient` public API so automation scripts can scroll a `ScrollPane` by a relative offset.

#### Scenario: Call scroll_by with delta and selector
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.2, id="myScrollPane")`
- **THEN** the Python client dispatches a `scroll_by` action with payload `{"delta_x": 0.0, "delta_y": 0.2, "selector": {"id": "myScrollPane"}}` to the Java agent

#### Scenario: Call scroll_by without selector
- **WHEN** an automation script calls `client.scroll_by(0.0, 0.5)` with no selector keyword arguments
- **THEN** the Python client dispatches the action with an empty selector, and the Java agent targets the first `ScrollPane` in the scene

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

### Requirement: Python client exposes Locator
The system SHALL provide a `Locator` class obtainable via `client.locator(**selector)` that stores a node selector and re-uses it on every subsequent call, removing selector repetition from automation scripts and Page Object Models.

#### Scenario: Obtain locator and call methods without repeating selector
- **WHEN** a script calls `loc = client.locator(id="loginBtn")` and then calls `loc.click()`, `loc.verify_text("Login")`, `loc.wait_for_visible()`
- **THEN** each call is dispatched to the underlying client with `id="loginBtn"` merged in automatically

#### Scenario: Locator raises ValueError when created with no selector
- **WHEN** a script calls `client.locator()` with no arguments
- **THEN** the Python client raises `ValueError` immediately

#### Scenario: Locator wait_for_* raises ValueError if no id= given
- **WHEN** a script creates a locator with `text=` only and calls `loc.wait_for_visible()`
- **THEN** the Python client raises `ValueError` with a message indicating `id=` is required

