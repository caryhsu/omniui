## ADDED Requirements

### Requirement: get_clipboard returns current clipboard text
The system SHALL provide a `get_clipboard()` method on `OmniUIClient` that sends a `get_clipboard` action to the Java agent and returns an `ActionResult` whose `value` is the current system clipboard text string (empty string if clipboard is empty or contains non-text content).

#### Scenario: Clipboard contains text
- **WHEN** the system clipboard contains the string `"Hello"`
- **THEN** `client.get_clipboard()` returns `ActionResult(ok=True, value="Hello")`

#### Scenario: Clipboard is empty
- **WHEN** the system clipboard is empty
- **THEN** `client.get_clipboard()` returns `ActionResult(ok=True, value="")`

### Requirement: set_clipboard writes text to clipboard
The system SHALL provide a `set_clipboard(text)` method on `OmniUIClient` that sends a `set_clipboard` action with the given text to the Java agent, placing that text on the system clipboard.

#### Scenario: Set and read back
- **WHEN** `client.set_clipboard("OmniUI")` is called
- **THEN** the system clipboard contains `"OmniUI"`
- **THEN** `client.get_clipboard().value` returns `"OmniUI"`

### Requirement: copy triggers select-all then copy on a node
The system SHALL provide a `copy(**selector)` method on `OmniUIClient` that issues `press_key("Control+A", **selector)` followed by `press_key("Control+C", **selector)`, selecting all content of the node and copying it to the clipboard.

#### Scenario: Copy text field content
- **WHEN** `client.copy(id="username")` is called on a TextField containing `"admin"`
- **THEN** the system clipboard contains `"admin"`

### Requirement: paste triggers paste on a node
The system SHALL provide a `paste(**selector)` method on `OmniUIClient` that issues `press_key("Control+V", **selector)`, pasting current clipboard content into the node.

#### Scenario: Paste into text field
- **WHEN** clipboard contains `"test_value"` and `client.paste(id="username")` is called
- **THEN** the TextField with id `"username"` contains `"test_value"`

### Requirement: clipboard actions exported from omniui package
`get_clipboard`, `set_clipboard`, `copy`, and `paste` SHALL be accessible as methods on `OmniUIClient` instances obtained from `OmniUI.connect()` or `OmniUI.launch()`.

#### Scenario: Methods present on client
- **WHEN** a connected `OmniUIClient` is obtained
- **THEN** `client.get_clipboard`, `client.set_clipboard`, `client.copy`, and `client.paste` are callable
