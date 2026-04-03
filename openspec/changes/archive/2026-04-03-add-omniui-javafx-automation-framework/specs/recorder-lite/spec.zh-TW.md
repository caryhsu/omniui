## ADDED Requirements

### Requirement: Recorder-lite click capture
系統必須支援一個可選的 recorder-lite mode，在受支援的本機環境下，於 recording session 期間捕捉使用者的 click action。

#### Scenario: 在 recording session 中捕捉 click
- **WHEN** recorder-lite 啟用，且使用者在 target application 上執行 click
- **THEN** 系統必須記錄該 click event，以供 selector mapping 與 script generation 使用

### Requirement: Selector-based script generation
當 recorded click 能映射到 JavaFX node 或其他受支援 selector source 時，recorder-lite 必須以穩定 selector 輸出 script。

#### Scenario: 從 JavaFX node mapping 輸出 click script
- **WHEN** recorded click 可映射到 `fx:id="loginButton"` 的 JavaFX node
- **THEN** recorder 必須輸出等價於 `click(id="loginButton")` 的 script line

### Requirement: No coordinate-only script emission
若 recorded click 無法推導出穩定 selector，recorder-lite 不得只根據 raw screen coordinates 輸出 script line。

#### Scenario: 抑制不穩定的 recording output
- **WHEN** recorded click 無法映射到 JavaFX selector、OCR text region 或其他受支援的穩定 selector 形式
- **THEN** recorder 必須略過該 click 的 script generation，而不是輸出 coordinate-based action

### Requirement: Phase 1 recorder scope limit
在 Phase 1 中，recorder-lite 必須限制為簡單的 click-oriented script generation，且不得宣稱支援 full workflow recording coverage。

#### Scenario: 忽略不支援的 interaction type
- **WHEN** recording session 中出現 drag-and-drop 或複雜 keyboard choreography 等不受支援互動
- **THEN** recorder 必須將該互動排除在 generated script output 之外，並回報其為 unsupported
