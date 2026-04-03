## Purpose

定義 recorder-lite 功能，用於擷取使用者互動並產生穩定的 automation script。

## Requirements

### Requirement: Recorder-lite click capture
系統 SHALL 支援選用的 recorder-lite 模式，在支援的本機上錄製 session 期間擷取使用者的點擊 action。

#### Scenario: Capture click during recording session
- **WHEN** recorder-lite 已啟用，且使用者在目標應用程式中執行點擊
- **THEN** 系統記錄該點擊事件，供 selector 對應與 script 產生使用

### Requirement: Selector-based script generation
當點擊目標可對應至 JavaFX node 或其他支援的 selector 來源時，recorder-lite SHALL 使用穩定的 selector 產生 script 輸出。

#### Scenario: Emit click script from JavaFX node mapping
- **WHEN** 一個錄製的點擊對應至 `fx:id="loginButton"` 的 JavaFX node
- **THEN** recorder 輸出等同於 `click(id="loginButton")` 的 script 行

### Requirement: No coordinate-only script emission
當無法為錄製的點擊推導出穩定的 selector 時，recorder-lite SHALL 不產生僅依賴原始螢幕座標的 script 行。

#### Scenario: Suppress unstable recording output
- **WHEN** 一個錄製的點擊無法對應至 JavaFX selector、OCR 文字區域或其他支援的穩定 selector 形式
- **THEN** recorder 省略該點擊的 script 產生，而不是輸出座標型 action

### Requirement: Phase 1 recorder scope limit
在 Phase 1，recorder-lite SHALL 僅限於簡單的點擊導向 script 產生，且 SHALL 不宣稱支援完整的工作流程錄製。

#### Scenario: Ignore unsupported interaction types
- **WHEN** 錄製 session 觀察到不支援的互動，例如拖放或複雜的鍵盤組合操作
- **THEN** recorder 將該互動排除於產生的 script 之外，並回報為不支援