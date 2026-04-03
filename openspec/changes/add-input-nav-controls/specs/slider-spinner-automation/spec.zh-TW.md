## ADDED Requirements

### Requirement: Slider 數值讀寫
系統 SHALL 支援讀取 `Slider` 節點的當前值，以及將其設為指定數值（需在 min/max 範圍內）。

#### Scenario: 讀取 Slider 值
- **WHEN** 自動化客戶端呼叫 `get_value(id="<sliderId>")`
- **THEN** 系統回傳 Slider 的當前 double 值

#### Scenario: 設定 Slider 值（在範圍內）
- **WHEN** 自動化客戶端呼叫 `set_slider(id="<sliderId>", value=<number>)`，
  且數值在 Slider 的 min/max 範圍內
- **THEN** Slider 的 value 屬性更新為指定數值

#### Scenario: 拒絕超出範圍的 Slider 值
- **WHEN** 自動化客戶端呼叫 `set_slider`，數值超出 Slider 的 min/max 範圍
- **THEN** 系統回傳失敗，reason 為 `value_out_of_range`

### Requirement: Spinner 數值讀寫與步進
系統 SHALL 支援讀取 `Spinner` 的當前值、設定指定值，以及以步進方式遞增或遞減。

#### Scenario: 讀取 Spinner 值
- **WHEN** 自動化客戶端呼叫 `get_value(id="<spinnerId>")`
- **THEN** 系統以字串形式回傳 Spinner 的當前值

#### Scenario: 設定 Spinner 值
- **WHEN** 自動化客戶端呼叫 `set_spinner(id="<spinnerId>", value="<val>")`
- **THEN** Spinner 的 value factory 更新為指定值

#### Scenario: 遞增 Spinner
- **WHEN** 自動化客戶端呼叫 `step_spinner(id="<spinnerId>", steps=<n>)`，`n` 為正整數
- **THEN** Spinner 遞增 `n` 步

#### Scenario: 遞減 Spinner
- **WHEN** 自動化客戶端呼叫 `step_spinner(id="<spinnerId>", steps=<n>)`，`n` 為負整數
- **THEN** Spinner 遞減 `abs(n)` 步
