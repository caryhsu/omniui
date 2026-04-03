## Why

目前的 JavaFX login demo 只覆蓋簡單的 text field、button 與 label，足以證明 Phase 1 vertical slice，但不足以驗證 OmniUI 面對真實 JavaFX control pattern 時的行為，例如 virtualized lists、selection-based controls、hierarchical views 與 table/grid 類元件。

## What Changes

- 新增進階 JavaFX demo 場景，涵蓋 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-like layout。
- 擴充 demo-driven validation，讓 OmniUI 可以驗證 selection、expansion 與 item-oriented interaction，而不只停留在簡單 click/type 流程。
- 釐清目前 JavaFX discovery 與 action model 對複雜控制項的適用範圍，以及哪些地方需要 contract 變更。
- 新增可執行的 Python demo，讓 richer UI structure 下的 discovery、action 與 fallback 行為可被直接觀察。

## Capabilities

### New Capabilities

- `advanced-javafx-demo-scenarios`: 提供 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-oriented layout 等複雜控制項的參考 JavaFX demo 場景。

### Modified Capabilities

- `javafx-automation-core`: JavaFX discovery 與 action requirement 需要擴充到更複雜的控制結構，包括 selection-driven 與 virtualized controls。
- `python-automation-client`: 若 `click` / `type` 不足以表達進階控制項互動，client contract 需要支援更高階的 interaction pattern。

## Impact

- 影響程式碼：`demo/javafx-login-app/`、`demo/python/`、`scripts/`，以及 `java-agent/` 中與 JavaFX adapter 相關的邏輯。
- 影響測試範圍：需要新增複雜 JavaFX 控制項的 scenario validation 與 regression check。
- 影響 API surface：可能需要新增或明確定義 selection-oriented action 與 richer control-state inspection。
