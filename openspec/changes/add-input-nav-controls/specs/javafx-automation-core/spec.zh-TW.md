## ADDED Requirements

### Requirement: get_selected action
系統 SHALL 將 `get_selected` action 路由至取得節點（RadioButton、ToggleButton、
CheckBox）的布林 `selected` 屬性。

#### Scenario: 對 RadioButton 執行 get_selected
- **WHEN** 對 RadioButton 節點呼叫 `perform("get_selected", selector, null)`
- **THEN** action 回傳 `ActionResult.success`，value 為節點的 `isSelected()` 布林值

### Requirement: set_selected action
系統 SHALL 將 `set_selected` action 路由至對節點呼叫 `setSelected(boolean)`。

#### Scenario: 對 RadioButton 或 ToggleButton 執行 set_selected
- **WHEN** 呼叫 `perform("set_selected", selector, {value: true/false})`
- **THEN** 在 FX thread 上呼叫 `node.setSelected(value)`，action 回傳成功

### Requirement: set_slider action
系統 SHALL 將 `set_slider` action 路由至在驗證範圍後呼叫 Slider 的 `setValue(double)`。

#### Scenario: set_slider 在範圍內
- **WHEN** 呼叫 `perform("set_slider", selector, {value: <number>})`，數值在 min/max 內
- **THEN** 呼叫 `slider.setValue(number)`，action 回傳成功

#### Scenario: set_slider 超出範圍
- **WHEN** 數值超出 min/max
- **THEN** action 回傳失敗，reason 為 `value_out_of_range`

### Requirement: set_spinner 與 step_spinner actions
系統 SHALL 將 `set_spinner` 路由至更新 Spinner value factory，
將 `step_spinner` 路由至呼叫 `increment` 或 `decrement`。

#### Scenario: set_spinner
- **WHEN** 呼叫 `perform("set_spinner", selector, {value: "<str>"})`
- **THEN** 呼叫 `spinner.getValueFactory().setValue(converted)`

#### Scenario: step_spinner 正數
- **WHEN** 呼叫 `perform("step_spinner", selector, {steps: <n>})`，n > 0
- **THEN** 呼叫 `spinner.increment(n)`

#### Scenario: step_spinner 負數
- **WHEN** 呼叫 `perform("step_spinner", selector, {steps: <n>})`，n < 0
- **THEN** 呼叫 `spinner.decrement(abs(n))`

### Requirement: get_progress action
系統 SHALL 將 `get_progress` 路由至回傳 ProgressBar 或 ProgressIndicator 的 `getProgress()` 值。

#### Scenario: get_progress
- **WHEN** 對 ProgressBar 呼叫 `perform("get_progress", selector, null)`
- **THEN** action 回傳成功，value 為 `node.getProgress()`

### Requirement: get_tabs 與 select_tab actions
系統 SHALL 將 `get_tabs` 路由至列舉 TabPane 的分頁，將 `select_tab` 路由至切換分頁。

#### Scenario: get_tabs
- **WHEN** 對 TabPane 呼叫 `perform("get_tabs", selector, null)`
- **THEN** action 回傳含 `text` 與 `disabled` 的分頁描述清單

#### Scenario: select_tab 找到
- **WHEN** 呼叫 `perform("select_tab", selector, {tab: "<title>"})` 且分頁存在
- **THEN** 呼叫 `tabPane.getSelectionModel().select(tab)`

#### Scenario: select_tab 找不到
- **WHEN** 指定分頁標題不存在
- **THEN** action 回傳失敗，reason 為 `tab_not_found`
