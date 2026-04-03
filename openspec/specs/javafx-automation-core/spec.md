## Purpose

Define the core JavaFX automation behavior exposed by OmniUI through its Java agent integration.

## Requirements

### Requirement: Java agent connectivity
The system SHALL provide a Java agent that can be injected into a supported local JVM hosting a JavaFX application and expose node discovery and action execution endpoints to the Python automation engine without requiring target app source modifications.

#### Scenario: Connect Python client to injected JavaFX target
- **WHEN** a supported JavaFX application is launched with the OmniUI Java agent on the local machine
- **THEN** the Python client can establish a session with the local agent and use it to query nodes and invoke supported actions

#### Scenario: Fail clearly when app is not agent-enabled
- **WHEN** the JavaFX application is launched without OmniUI Java agent injection
- **THEN** the Python client cannot establish a session and receives a clear connection failure instead of silently binding to a built-in demo target

### Requirement: JavaFX node discovery
The system SHALL provide a JavaFX discovery capability that enumerates the active scene graph of the target application from agent-discovered runtime state and returns node records containing `fx:id` when present, node type, text content when present, and hierarchy path, including visible state for advanced controls such as popup-backed, hierarchical, and virtualized controls.

#### Scenario: Enumerate visible login form nodes from injected agent
- **WHEN** the Python client requests `get_nodes()` against an agent-enabled JavaFX application
- **THEN** the system returns node records discovered from the live JavaFX runtime without requiring the application to register its own stage with OmniUI

#### Scenario: Discover visible advanced-control state
- **WHEN** the Python client requests `get_nodes()` while advanced JavaFX demo scenarios are visible
- **THEN** the system returns node records for the currently materialized control state, including visible `ComboBox`, `ListView`, `TreeView`, and `TableView` structures

### Requirement: Priority-based selector resolution
The system SHALL resolve selectors for JavaFX actions in the following order: exact `fx:id`, structural match using node type and text, OCR text match, and vision template match.

#### Scenario: Resolve button by fx:id before any fallback
- **WHEN** a script calls `click(id="loginButton")` and a JavaFX node exists with `fx:id="loginButton"`
- **THEN** the system resolves the target through the JavaFX adapter without invoking OCR or vision matching

#### Scenario: Resolve by type and text when fx:id is absent
- **WHEN** a script calls `click(text="Login", type="Button")` and no matching `fx:id` selector is provided
- **THEN** the system resolves the target to the JavaFX button whose type and visible text match the selector

### Requirement: Direct JavaFX interaction
The system SHALL execute supported actions against JavaFX-resolved nodes through runtime event dispatch or equivalent node-level interaction, not through screen coordinates.

#### Scenario: Click resolved JavaFX button without mouse coordinates
- **WHEN** a JavaFX node has been resolved for a click action
- **THEN** the system triggers the click through the JavaFX runtime on the application thread without requiring OS-level pointer movement

### Requirement: JavaFX action surface
The system SHALL support `click`, `type`, `get_text`, and `verify_text` operations for JavaFX-resolved elements through the Python API, as well as the extended action set for advanced controls.

#### Scenario: Execute login flow through JavaFX selectors
- **WHEN** a script issues click, type, and verify operations against JavaFX-resolved username, password, login button, and status elements
- **THEN** the system completes the flow and reports the expected status text without requiring fallback resolution

### Requirement: Advanced JavaFX interaction coverage
The system SHALL support automation of representative advanced JavaFX control interactions needed by the reference demos, including selection-oriented and hierarchy-oriented operations where required by the supported scenarios.

#### Scenario: Select visible list item in advanced demo
- **WHEN** a supported advanced demo scenario requires selecting a visible item in a selection-based JavaFX control
- **THEN** the system can perform the interaction through the JavaFX runtime and verify the resulting selected value or state

#### Scenario: Expand or select visible tree item in advanced demo
- **WHEN** a supported advanced demo scenario requires expanding or selecting a visible tree node
- **THEN** the system can drive the required JavaFX interaction and observe the resulting visible state

### Requirement: Action observability
The system SHALL report action diagnostics for every executed operation, including the requested selector, resolution tier, fallback tier used, and confidence score for OCR or vision paths when applicable.

#### Scenario: Log structural action diagnostics
- **WHEN** a script executes a click that resolves through JavaFX structure
- **THEN** the action result includes the original selector, resolution tier `javafx`, and indicates that no OCR or vision confidence score was required

### Requirement: Overlay window node discovery
The system SHALL enumerate nodes from all open overlay windows — including `PopupWindow` instances (ContextMenu, MenuBar popup, DatePicker popup) and Dialog or Alert `Stage` instances — in addition to the primary scene graph, when a query is made while an overlay is visible.

#### Scenario: Discover nodes in a visible ContextMenu overlay
- **WHEN** a ContextMenu is open and the Python client calls `get_nodes()`
- **THEN** the system returns node records for the ContextMenu's `MenuItem` nodes from the overlay window in addition to primary scene nodes

#### Scenario: Discover nodes in an open Dialog
- **WHEN** a modal Dialog is visible and the Python client calls `get_nodes()`
- **THEN** the system returns node records from the `DialogPane` scene including title node, content node, and button nodes

#### Scenario: Discover nodes in an open DatePicker popup
- **WHEN** a DatePicker calendar popup is open and the Python client calls `get_nodes()`
- **THEN** the system returns node records for the calendar popup's date cells and navigation buttons in addition to primary scene nodes

### Requirement: Wait-for-overlay readiness
The system SHALL provide a mechanism that polls the open window list after a popup-triggering action until a new overlay window matching the expected type appears, before returning control to the Python client.

#### Scenario: Wait for ContextMenu popup to appear after right-click
- **WHEN** the Python client dispatches a right-click that triggers a ContextMenu
- **THEN** the system waits until a ContextMenu `PopupWindow` appears in the open window list before reporting success

#### Scenario: Report timeout when overlay does not appear
- **WHEN** the Python client dispatches a right-click on a node with no ContextMenu and waits for an overlay popup
- **THEN** the system reports a timeout error after the configured wait duration without returning partial results

### Requirement: get_selected and set_selected actions
The system SHALL route `get_selected` to return the boolean selected property and `set_selected` to invoke `setSelected(boolean)` on nodes that support it (RadioButton, ToggleButton, CheckBox).

#### Scenario: get_selected on RadioButton
- **WHEN** `perform("get_selected", selector, null)` is called on a RadioButton node
- **THEN** the action returns `ActionResult.success` with value equal to the node's `isSelected()` boolean

#### Scenario: set_selected on RadioButton or ToggleButton
- **WHEN** `perform("set_selected", selector, {value: true/false})` is called
- **THEN** `node.setSelected(value)` is invoked on the FX thread and the action returns success

### Requirement: set_slider action
The system SHALL route `set_slider` to call `setValue(double)` on a Slider node after validating the value is within min/max range.

#### Scenario: set_slider within bounds
- **WHEN** `perform("set_slider", selector, {value: <number>})` is called with a value within the Slider's min/max
- **THEN** `slider.setValue(number)` is called and the action returns success

#### Scenario: set_slider out of bounds
- **WHEN** the value is outside min/max
- **THEN** the action returns failure with reason `value_out_of_range`

### Requirement: set_spinner and step_spinner actions
The system SHALL route `set_spinner` to update the Spinner's value factory and `step_spinner` to call `increment` or `decrement` with the given steps.

#### Scenario: set_spinner
- **WHEN** `perform("set_spinner", selector, {value: "<str>"})` is called
- **THEN** `spinner.getValueFactory().setValue(converted)` is invoked

#### Scenario: step_spinner positive
- **WHEN** `perform("step_spinner", selector, {steps: <n>})` is called with n > 0
- **THEN** `spinner.increment(n)` is called

#### Scenario: step_spinner negative
- **WHEN** `perform("step_spinner", selector, {steps: <n>})` is called with n < 0
- **THEN** `spinner.decrement(abs(n))` is called

### Requirement: get_progress action
The system SHALL route `get_progress` to return the `getProgress()` value of a ProgressBar or ProgressIndicator node.

#### Scenario: get_progress
- **WHEN** `perform("get_progress", selector, null)` is called on a ProgressBar
- **THEN** the action returns success with value equal to `node.getProgress()`

### Requirement: get_tabs and select_tab actions
The system SHALL route `get_tabs` to enumerate tabs of a TabPane and `select_tab` to switch to a named tab.

#### Scenario: get_tabs
- **WHEN** `perform("get_tabs", selector, null)` is called on a TabPane
- **THEN** the action returns a list of tab descriptors with `text` and `disabled`

#### Scenario: select_tab found
- **WHEN** `perform("select_tab", selector, {tab: "<title>"})` is called and the tab exists
- **THEN** `tabPane.getSelectionModel().select(tab)` is called

#### Scenario: select_tab not found
- **WHEN** the specified tab title is not found
- **THEN** the action returns failure with reason `tab_not_found`

### Requirement: TextArea and PasswordField text actions
`TextArea` and `PasswordField` nodes are supported for `get_text` and `type` actions via their inherited `TextInputControl.getText()` and `setText()` methods. No additional branching is required.

### Requirement: Hyperlink click and visited state
`Hyperlink` nodes support `click` via the existing `handleClick`/`fire()` path. The `get_visited` action returns the `isVisited()` boolean property.

#### Scenario: Click Hyperlink and verify visited
- **WHEN** `click` is called on a Hyperlink node
- **THEN** `fire()` is invoked and `isVisited()` returns `true`

