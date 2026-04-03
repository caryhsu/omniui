## ADDED Requirements

### Requirement: get_selected action
The system SHALL route the `get_selected` action to return the boolean `selected`
property of the matched node (RadioButton, ToggleButton, CheckBox).

#### Scenario: get_selected on RadioButton
- **WHEN** `perform("get_selected", selector, null)` is called on a RadioButton node
- **THEN** the action returns `ActionResult.success` with value equal to the
  node's `isSelected()` boolean

### Requirement: set_selected action
The system SHALL route the `set_selected` action to invoke `setSelected(boolean)`
on the matched node.

#### Scenario: set_selected on RadioButton or ToggleButton
- **WHEN** `perform("set_selected", selector, {value: true/false})` is called
- **THEN** `node.setSelected(value)` is invoked on the FX thread and the action
  returns success

### Requirement: set_slider action
The system SHALL route the `set_slider` action to call `setValue(double)` on a
Slider node after validating the value is within the Slider's min/max range.

#### Scenario: set_slider within bounds
- **WHEN** `perform("set_slider", selector, {value: <number>})` is called with
  a value within the Slider's min/max
- **THEN** `slider.setValue(number)` is called and the action returns success

#### Scenario: set_slider out of bounds
- **WHEN** the value is outside min/max
- **THEN** the action returns failure with reason `value_out_of_range`

### Requirement: set_spinner and step_spinner actions
The system SHALL route `set_spinner` to update the Spinner's value factory and
`step_spinner` to call `increment` or `decrement` with the given steps.

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
The system SHALL route `get_progress` to return the `getProgress()` value of a
ProgressBar or ProgressIndicator node.

#### Scenario: get_progress
- **WHEN** `perform("get_progress", selector, null)` is called on a ProgressBar
- **THEN** the action returns success with value equal to `node.getProgress()`

### Requirement: get_tabs and select_tab actions
The system SHALL route `get_tabs` to enumerate tabs of a TabPane and `select_tab`
to switch to a named tab.

#### Scenario: get_tabs
- **WHEN** `perform("get_tabs", selector, null)` is called on a TabPane
- **THEN** the action returns a list of tab descriptors with `text` and `disabled`

#### Scenario: select_tab found
- **WHEN** `perform("select_tab", selector, {tab: "<title>"})` is called and the
  tab exists
- **THEN** `tabPane.getSelectionModel().select(tab)` is called

#### Scenario: select_tab not found
- **WHEN** the specified tab title is not found
- **THEN** the action returns failure with reason `tab_not_found`
