## ADDED Requirements

### Requirement: focus action moves keyboard focus to a node
The system SHALL provide a `focus` action that calls `requestFocus()` on the resolved JavaFX node, making it the keyboard focus owner of its scene.

#### Scenario: Focus a focusable node by id
- **WHEN** an automation script calls `focus(id="username")`
- **THEN** the agent calls `requestFocus()` on the node and returns `{"action":"focus","status":"ok"}`

#### Scenario: Selector not found
- **WHEN** an automation script calls `focus(id="nonExistent")`
- **THEN** the agent returns a failure result with `reason="selector_not_found"`

### Requirement: get_focused action returns the current focus owner
The system SHALL provide a `get_focused` action that returns the `fx:id` and node type of the scene's current focus owner. If no node is focused, `fxId` SHALL be `null`.

#### Scenario: A node is focused
- **WHEN** an automation script calls `get_focused()` and a node with `fx:id="username"` is focused
- **THEN** the agent returns `{"fxId": "username", "nodeType": "TextField"}`

#### Scenario: No node is focused
- **WHEN** no node currently holds focus
- **THEN** the agent returns `{"fxId": null, "nodeType": null}`

### Requirement: Python focus() method
The Python client SHALL expose a `focus(**selector)` method that sends the `focus` action to the agent and returns the result dict.

#### Scenario: focus by id
- **WHEN** a Python script calls `client.focus(id="username")`
- **THEN** an HTTP request is sent with `action="focus"` and `selector={"id":"username"}`

### Requirement: Python tab_focus() method
The Python client SHALL expose a `tab_focus(times=1, reverse=False)` method that presses Tab (or Shift+Tab) the specified number of times using the existing `press_key` action.

#### Scenario: Tab forward once
- **WHEN** `client.tab_focus()` is called
- **THEN** one `press_key("Tab")` action is sent

#### Scenario: Tab backward twice
- **WHEN** `client.tab_focus(times=2, reverse=True)` is called
- **THEN** two `press_key("Shift+Tab")` actions are sent

### Requirement: Python get_focused() method
The Python client SHALL expose a `get_focused()` method that sends the `get_focused` action and returns `{"fxId": ..., "nodeType": ...}`.

#### Scenario: Returns focused node info
- **WHEN** `client.get_focused()` is called
- **THEN** the result dict contains `fxId` and `nodeType` keys

### Requirement: Python verify_focused() assertion
The Python client SHALL expose a `verify_focused(id=...)` method that asserts the current focus owner matches the given selector. It SHALL raise `AssertionError` if the focused node's fxId does not match.

#### Scenario: Focus matches expectation
- **WHEN** `client.verify_focused(id="username")` is called and `username` is focused
- **THEN** the method returns without error

#### Scenario: Focus does not match
- **WHEN** `client.verify_focused(id="username")` is called but `password` is focused
- **THEN** an `AssertionError` is raised with a descriptive message
