## Purpose

定義當 JavaFX 結構比對不可用時，selector 解析的多模態 fallback 解析行為。

## Requirements

### Requirement: OCR fallback resolution
當請求的元素無法從 JavaFX 結構解析，且 selector 可透過可見文字比對時，系統 SHALL 執行基於 OCR 的 selector 解析。

#### Scenario: Click login button through OCR fallback
- **WHEN** 一個 click action 無法解析 JavaFX node，且畫面包含符合所請求 selector 的可見文字
- **THEN** 系統擷取截圖、執行 OCR、解析符合的文字區域，並對該區域執行點擊

### Requirement: Vision fallback resolution
只有在 JavaFX 結構解析與 OCR 文字解析均失敗後，系統 SHALL 才執行基於 template matching 的解析。

#### Scenario: Match element by template after OCR fails
- **WHEN** 請求的目標無法從 JavaFX 結構解析，且 OCR 未識別到符合的文字區域
- **THEN** 系統以視覺 template matching 作為在宣告 action 無法解析前的最終 fallback 步驟

### Requirement: Deterministic fallback ordering
對每次 selector 解析嘗試，系統 SHALL 維持 JavaFX 結構優先、OCR 其次、視覺最後的確定性 fallback 鏈。

#### Scenario: Record fallback chain in action result
- **WHEN** 一個 action 透過視覺 matching 解析
- **THEN** action result 顯示在視覺層成功前，已嘗試過 JavaFX 與 OCR 解析

### Requirement: Confidence reporting for fallback tiers
系統 SHALL 在 action result 與 debug 輸出中，為 OCR 與視覺解析加入信心分數。

#### Scenario: Return OCR confidence metadata
- **WHEN** 一個 click action 透過 OCR 文字比對解析
- **THEN** 系統回傳 OCR 信心分數，以及所使用的符合文字區域與 selector

### Requirement: Local performance bound for fallback execution
在 Phase 1 接受的本機運作條件下，系統 SHALL 在一秒內完成 OCR fallback 解析嘗試。

#### Scenario: OCR completes within acceptable bound
- **WHEN** 目標畫面狀態穩定，且 automation engine 在支援的本機上呼叫 OCR fallback
- **THEN** OCR 解析步驟在一秒內完成，然後再進行 action 分派或失敗回報