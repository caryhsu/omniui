## Purpose

定義 JavaFX Slider 與 Spinner 控制項的 automation 行為。

## Requirements

### Requirement: Slider value read and write
系統 SHALL 支援讀取 `Slider` node 的目前值，並將其設定為 slider min/max 範圍內的指定數值。

#### Scenario: Read Slider value
- **WHEN** automation client 呼叫 `get_value(id="<sliderId>")`
- **THEN** 系統回傳 Slider 目前的 double 值

#### Scenario: Set Slider value within range
- **WHEN** automation client 呼叫 `set_slider(id="<sliderId>", value=<number>)`，且值在 Slider 的 min/max 範圍內
- **THEN** Slider 的 value 屬性更新為指定數值

#### Scenario: Reject Slider value out of range
- **WHEN** automation client 以超出 Slider min/max 範圍的值呼叫 `set_slider`
- **THEN** 系統回傳失敗結果，reason 為 `value_out_of_range`

### Requirement: Spinner value read, write and step
系統 SHALL 支援讀取 `Spinner` 的目前值、設定指定值，以及以給定步數遞增或遞減。

#### Scenario: Read Spinner value
- **WHEN** automation client 呼叫 `get_value(id="<spinnerId>")`
- **THEN** 系統以字串回傳 Spinner 的目前值

#### Scenario: Set Spinner value
- **WHEN** automation client 呼叫 `set_spinner(id="<spinnerId>", value="<val>")`
- **THEN** Spinner 的 value factory 更新為指定值

#### Scenario: Increment Spinner
- **WHEN** automation client 以正整數 `n` 呼叫 `step_spinner(id="<spinnerId>", steps=<n>)`
- **THEN** Spinner 遞增 `n` 步

#### Scenario: Decrement Spinner
- **WHEN** automation client 以負整數 `n` 呼叫 `step_spinner(id="<spinnerId>", steps=<n>)`
- **THEN** Spinner 遞減 `abs(n)` 步