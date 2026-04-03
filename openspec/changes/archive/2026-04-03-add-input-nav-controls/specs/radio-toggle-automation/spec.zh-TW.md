## ADDED Requirements

### Requirement: RadioButton 選取
系統 SHALL 支援讀取 `RadioButton` 節點的選取狀態，以及透過自動化 action 將其設為選取。

#### Scenario: 讀取 RadioButton 選取狀態
- **WHEN** 自動化客戶端呼叫 `get_selected(id="<radioButtonId>")`
- **THEN** 系統回傳 `true`（已選取）或 `false`（未選取）

#### Scenario: 選取 RadioButton
- **WHEN** 自動化客戶端呼叫 `set_selected(id="<radioButtonId>", value=True)`
- **THEN** 該 RadioButton 變為選取，同一 ToggleGroup 內的其他 RadioButton
  由 JavaFX 自動取消選取

### Requirement: ToggleButton 選取
系統 SHALL 支援讀取與切換 `ToggleButton` 的選取狀態。

#### Scenario: 讀取 ToggleButton 選取狀態
- **WHEN** 自動化客戶端呼叫 `get_selected(id="<toggleButtonId>")`
- **THEN** 系統回傳 ToggleButton 當前的布林選取狀態

#### Scenario: 設定 ToggleButton 為選取
- **WHEN** 自動化客戶端呼叫 `set_selected(id="<toggleButtonId>", value=True)`
- **THEN** ToggleButton 的 selected 屬性變為 `true`

#### Scenario: 設定 ToggleButton 為未選取
- **WHEN** 自動化客戶端呼叫 `set_selected(id="<toggleButtonId>", value=False)`
- **THEN** ToggleButton 的 selected 屬性變為 `false`
