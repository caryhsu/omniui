## 1. 專案與 protocol 基礎

- [x] 1.1 建立 OmniUI 模組結構，包含 core engine、Python client、Java agent、selector engine、OCR、vision 與 recorder-lite
- [x] 1.2 定義本機 agent protocol，涵蓋 node discovery、action execution、screenshot retrieval 與 health/session management
- [x] 1.3 新增 contract test 或 fixture，驗證 Python client 與 Java agent 之間的 request / response payload

## 2. JavaFX automation core

- [x] 2.1 實作 Java agent attachment 與 JavaFX runtime hook，供受支援的本機 target app 使用
- [x] 2.2 實作 scene graph enumeration，回傳 `fx:id`、node type、text content、hierarchy path 與相關 state metadata
- [x] 2.3 實作 direct JavaFX action execution，支援對 resolved node 執行 `click`、`type`、`get_text`
- [x] 2.4 實作 Python client 高階 API：`connect`、`get_nodes`、`click`、`type`、`get_text`、`verify_text`

## 3. Selector resolution 與 fallback chain

- [x] 3.1 實作 selector normalization 與 priority-based resolution，優先 `fx:id`，其次 `type + text`
- [x] 3.2 實作 OCR fallback，使用 screenshot 與 text-region lookup
- [x] 3.3 實作 vision template matching，作為 JavaFX 與 OCR 失敗後的最後 fallback
- [x] 3.4 實作 normalized resolved-element result model，包含 selector source、tier、target reference 與 confidence metadata

## 4. Observability 與 reliability

- [x] 4.1 加入 action tracing，記錄 selector input、resolution tier、attempted fallback 與 OCR / vision confidence
- [x] 4.2 加入 stale JavaFX node reference 的 retry 或 re-resolution handling
- [x] 4.3 量測並調整 JavaFX node query 與 OCR fallback performance，使其符合 Phase 1 目標

## 5. Recorder-lite 與 demo 驗證

- [x] 5.1 實作 recorder-lite click capture 與 JavaFX-backed interaction selector mapping
- [x] 5.2 只對能以穩定 selector 表達的 click 產生 script output
- [x] 5.3 建立或接入 reference JavaFX login app，作為 end-to-end validation target
- [x] 5.4 新增 automated demo script，驗證 login flow、JavaFX direct interaction 與 OCR fallback behavior
