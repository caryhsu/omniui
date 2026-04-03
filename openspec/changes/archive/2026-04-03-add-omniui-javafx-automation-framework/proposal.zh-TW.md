## Why

自動化 Java 桌面應用的團隊，通常太早退回到螢幕座標層級，導致測試在 DPI、解析度與版面變動下變得脆弱。OmniUI 需要提供一條以 JavaFX 為優先的 automation path，能先從 scene graph 解析元素，只有在 runtime 結構不可用時才退回 OCR 或 vision。

這個 change 現在就需要被定義，因為在真正實作前，必須先把跨平台 UI automation framework 的 MVP 邊界固定下來。此 proposal 定義了一個聚焦 JavaFX 的 Phase 1，使其足以支援 login-flow 類型的自動化，同時保留未來擴充到 Swing、Web 與 native target 的乾淨延展路徑。

## What Changes

- 引入一個聚焦本機 JavaFX automation 的 OmniUI Phase 1 MVP
- 定義 Python client API，用於連線 target app、discover node、resolve selector、執行 action 與驗證結果
- 引入一個 Java agent，可 attach 到 target JVM、檢查 JavaFX scene graph，並在可行時以 event-level interaction 執行操作，而不依賴螢幕座標
- 定義 selector resolution pipeline，優先使用 JavaFX structural selector，其次為 OCR text match，最後為 vision template match
- 定義 action execution、debug observability，以及穩定 JavaFX login-flow demo 的 acceptance criteria
- 將 lightweight recorder 列為 Phase 1 可選能力，在 node mapping 可用時能捕捉 click 並輸出簡單 automation script line

## Capabilities

### New Capabilities
- `javafx-automation-core`: 透過 attach 的 agent discover JavaFX node、resolve selector，並執行 direct JavaFX interaction
- `multimodal-fallback-resolution`: 當 JavaFX 結構不可用時，透過 OCR 與 vision fallback resolve action
- `python-automation-client`: 提供 Python API，用於 connection、discovery、action execution 與 verification flow
- `recorder-lite`: 捕捉基本 click interaction，並在受支援情境下輸出簡單 Python automation script

### Modified Capabilities
- None.

## Impact

- 影響的 code 會包含新的 `omniui/` 模組，用於 core orchestration、selector logic、OCR、vision、Python client、Java agent 與 recorder-lite
- 將引入 Python client 與 Java agent 之間的 inter-process protocol；Phase 1 可採用 HTTP、socket 或 gRPC，最終選型在 design 中決定
- 會增加 OCR 與 template matching fallback pipeline 所需的依賴
- 會建立本機 JavaFX automation 的行為契約與 acceptance criteria，包括 selector source、fallback tier 與 confidence score 等 observability 資訊
