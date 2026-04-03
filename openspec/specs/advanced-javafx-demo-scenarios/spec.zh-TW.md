## Purpose

定義參考用的進階 JavaFX demo 場景，用來驗證基本 login flow 之外的控制項 discovery 與 interaction 行為。

## Requirements

### Requirement: Advanced JavaFX demo coverage
系統 SHALL 提供超出基本 login flow 的參考 JavaFX demo 場景，涵蓋 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-oriented layout。

#### Scenario: Open advanced demo scenarios
- **WHEN** 使用者啟動 reference JavaFX demo app
- **THEN** app 會暴露可辨識的進階控制項場景，而不只有基本 login flow

### Requirement: Stable demo data for advanced controls
系統 SHALL 在進階 demo 場景中使用 deterministic、可讀的 sample data，讓 structural discovery 與 automation assertion 保持可理解性。

#### Scenario: Inspect advanced control dataset
- **WHEN** 使用者或 Python client 檢視進階場景
- **THEN** 可見的 options、rows、items 與 tree labels 都是穩定、唯一且適合做 selector-based automation 的

### Requirement: Runnable advanced Python demos
系統 SHALL 提供可執行的 Python demo，覆蓋進階 JavaFX 場景，並輸出足夠資訊以觀察 discovery 與 action behavior。

#### Scenario: Run advanced control demo script
- **WHEN** 使用者對 agent-enabled app 執行進階 JavaFX scenario 的 Python demo
- **THEN** script 會執行具代表性的控制項互動，並回報對應的 discovery 或 action trace
