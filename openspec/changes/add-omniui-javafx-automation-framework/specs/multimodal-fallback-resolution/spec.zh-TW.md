## ADDED Requirements

### Requirement: OCR fallback resolution
當 requested element 無法從 JavaFX structure resolve，且 selector 可透過可見文字匹配時，系統必須執行 OCR-based selector resolution。

#### Scenario: 透過 OCR fallback 點擊 login button
- **WHEN** click action 無法 resolve 到 JavaFX node，且畫面上存在與 requested selector 相符的可見文字
- **THEN** 系統必須截圖、執行 OCR、resolve 對應 text region，並對該 region 執行 click

### Requirement: Vision fallback resolution
系統必須在 JavaFX structural resolution 與 OCR text resolution 都失敗後，才執行 template-matching based resolution。

#### Scenario: OCR 失敗後用 template match 找 element
- **WHEN** requested target 無法從 JavaFX structure resolve，且 OCR 也找不到匹配文字區塊
- **THEN** 系統必須將 vision template matching 作為最後 fallback，若仍失敗才宣告 action unresolved

### Requirement: Deterministic fallback ordering
系統對每一次 selector resolution 嘗試都必須維持固定 fallback chain：JavaFX structure 第一、OCR 第二、vision 第三。

#### Scenario: Action result 記錄 fallback chain
- **WHEN** 一個 action 最終透過 vision matching resolve
- **THEN** action result 必須顯示 JavaFX 與 OCR resolution 已先被嘗試，之後 vision tier 才成功

### Requirement: Confidence reporting for fallback tiers
系統必須在 action result 與 debug output 中包含 OCR 與 vision-based resolution 的 confidence score。

#### Scenario: 回傳 OCR confidence metadata
- **WHEN** click action 透過 OCR text matching resolve
- **THEN** 系統必須一併回傳 OCR confidence score、matched text region 與 selector used

### Requirement: Local performance bound for fallback execution
在 Phase 1 接受的本機運行條件下，系統必須在一秒內完成一次 OCR fallback resolution 嘗試。

#### Scenario: OCR 在可接受時間內完成
- **WHEN** target screen state 穩定，且 automation engine 對受支援的本機環境啟動 OCR fallback
- **THEN** OCR resolution step 必須在一秒內完成，然後才進行 action dispatch 或 failure reporting
