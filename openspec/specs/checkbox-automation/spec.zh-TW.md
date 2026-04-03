## Purpose

定義 JavaFX CheckBox 控制項的 automation 行為。

## Requirements

### Requirement: CheckBox selected state read
系統 SHALL 支援讀取 `CheckBox` node 的目前勾選狀態，若已勾選回傳 `true`，否則回傳 `false`。

#### Scenario: Read a checked CheckBox
- **WHEN** automation client 以 `get_selected(id="<checkBoxId>")` 對已勾選的 CheckBox 呼叫
- **THEN** 系統回傳 `true`

#### Scenario: Read an unchecked CheckBox
- **WHEN** automation client 以 `get_selected(id="<checkBoxId>")` 對未勾選的 CheckBox 呼叫
- **THEN** 系統回傳 `false`

### Requirement: CheckBox selected state write
系統 SHALL 支援將 `CheckBox` node 的勾選狀態設定為指定的布林值。

#### Scenario: Check a CheckBox
- **WHEN** automation client 呼叫 `set_selected(id="<checkBoxId>", value=True)`
- **THEN** CheckBox 變為已勾選（`isSelected()` 回傳 `true`）

#### Scenario: Uncheck a CheckBox
- **WHEN** automation client 呼叫 `set_selected(id="<checkBoxId>", value=False)`
- **THEN** CheckBox 變為未勾選（`isSelected()` 回傳 `false`）

#### Scenario: CheckBoxes are independent
- **WHEN** automation client 將多個 CheckBox 設定為不同狀態
- **THEN** 每個 CheckBox 各自保持其狀態（CheckBox 不像同一 ToggleGroup 中的 RadioButton 具有互斥性）
