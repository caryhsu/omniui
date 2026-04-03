## Purpose

定義 JavaFX RadioButton 與 ToggleButton 控制項的 automation 行為。

## Requirements

### Requirement: RadioButton selection
系統 SHALL 支援讀取 `RadioButton` node 的選取狀態，並透過 automation action 將其設為已選取。

#### Scenario: Read RadioButton selected state
- **WHEN** automation client 呼叫 `get_selected(id="<radioButtonId>")`
- **THEN** 若 RadioButton 已選取則系統回傳 `true`，否則回傳 `false`

#### Scenario: Select a RadioButton
- **WHEN** automation client 呼叫 `set_selected(id="<radioButtonId>", value=True)`
- **THEN** RadioButton 變為已選取，且同一 ToggleGroup 中的其他 RadioButton 由 JavaFX 自動取消選取

### Requirement: ToggleButton selection
系統 SHALL 支援讀取及切換 `ToggleButton` 的選取狀態。

#### Scenario: Read ToggleButton selected state
- **WHEN** automation client 呼叫 `get_selected(id="<toggleButtonId>")`
- **THEN** 系統回傳 ToggleButton 目前的布林選取狀態

#### Scenario: Toggle ToggleButton state
- **WHEN** automation client 呼叫 `set_selected(id="<toggleButtonId>", value=True)`
- **THEN** ToggleButton 的 selected 屬性變為 `true`

#### Scenario: Set ToggleButton to deselected
- **WHEN** automation client 呼叫 `set_selected(id="<toggleButtonId>", value=False)`
- **THEN** ToggleButton 的 selected 屬性變為 `false`