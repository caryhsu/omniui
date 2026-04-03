## Context

OmniUI Phase 1 瞄準的是一個困難但可切入的 automation 問題：在單機環境中，對 JavaFX 桌面應用提供可靠的 UI automation。關鍵限制是，當 JavaFX runtime 能暴露 node identity 與 event dispatch path 時，framework 必須避免退回 coordinate-driven automation。OCR 與 vision 是 fallback 機制，不是對等主路徑。

這個 MVP 橫跨多個模組：Python client API、automation engine、attach 到 target JVM 的 Java agent、selector resolution logic、OCR / vision fallback service，以及 lightweight recorder。因此設計上需要明確的 Python control plane 與 Java agent 之間契約，並保證所有 action path 都有一致的 observability。

主要限制：
- Phase 1 只支援 JavaFX
- controller 端使用 Python
- target app 只考慮本機執行
- 優先使用 direct JavaFX interaction，而不是滑鼠座標層級操作
- OCR fallback 對常見情境必須維持在一秒內可接受
- 架構未來必須能延伸到 Swing、Web、native app，而不需要重寫 Python API surface

## Goals / Non-Goals

**Goals:**
- 提供穩定的 Python API，支援 connect、discover、find、click、type、get_text、verify_text、screenshot、OCR、vision-match
- attach Java agent 到 target JVM，並透過本機 RPC 介面回傳 JavaFX node tree 與執行 node-bound action
- 定義 deterministic selector resolution engine：`fx:id`、`type + text`、OCR text、vision template
- 標準化 action result 與 debug trace，使每個 action 都能回報 selector input、resolution tier、fallback 使用情況與 confidence
- 保持 adapter 邊界乾淨，讓 JavaFX 成為未來 multi-platform automation architecture 的第一個 concrete adapter
- 支援 recorder-lite，只在 click target 能被映射回可 resolve selector 時才輸出簡單 Python script line

**Non-Goals:**
- Phase 1 不處理 Swing、browser、native desktop automation
- 不做 multi-app orchestration、remote execution 或 distributed worker
- 不做 deep-learning based vision model 或 semantic detection
- 不做 full recorder/playback、drag-and-drop recording、複雜 keyboard macro recording
- 不把 OS-level event synthesis 當作 JavaFX-discoverable node 的主互動機制

## Decisions

### 1. Python client 與 Java agent 之間使用本機 HTTP JSON protocol

Phase 1 agent 採用 loopback-only HTTP API 與 JSON payload，而不是 gRPC 或 raw socket。

理由：
- 對 Python 與 Java 橫跨的 MVP 來說，更快落地、更容易 debug
- 容易透過 log 與 demo scenario 觀察 request / response
- 對本機延遲需求已足夠

備選方案：
- gRPC：契約更強，但會增加 code generation 與 setup 成本
- raw socket：runtime footprint 較小，但 protocol 可觀測性與維護性較差

### 2. 將 automation engine 建模為 adapter 加 fallback service，統一收斂在同一個 selector resolver 後面

Python automation engine 對外只暴露一套 action API，內部協調：
- JavaFX adapter：structural discovery 與 direct interaction
- OCR fallback service：從 screenshot 解析文字
- vision fallback service：template matching

理由：
- 對外可維持穩定 client API，同時允許 backend resolution strategy 演進
- 未來的 Swing / Web / native adapter 也能沿用相同 orchestration shape

備選方案：
- 讓 Python client 顯式選 backend：拒絕，因為會把 engine 複雜度洩漏到 script surface
- 將 structure 與 image automation 拆成不同 engine：拒絕，因為 fallback chaining 與 observability 會不一致

### 3. 將所有 resolved target 正規化為 `ResolvedElement`

Selector resolution 的結果統一包含：
- `tier`: `javafx`、`ocr`、`vision`
- `selector_used`
- `matched_attributes`
- `confidence`
- `target_ref`
- `debug_context`

理由：
- action execution 不必依賴 target 是如何被找到的
- 能提供一致的 log、retry、acceptance assertion
- 避免 OCR / vision 變成 public API 裡的特殊分支

備選方案：
- 回傳 backend-specific result type：拒絕，因為 action 與 debug logic 很快會分裂

### 4. 優先使用 event-level JavaFX action，而不是 input-device synthesis

當 JavaFX node 被 resolve 後，agent 應透過 JavaFX application thread 觸發 node-level behavior，而不是發出 OS 滑鼠點擊。

理由：
- 避免 DPI 與座標不穩定
- 在視覺不穩定或類 headless 狀態下仍能維持 deterministic behavior
- 直接滿足「結構化互動優先」這個核心非功能需求

備選方案：
- 永遠把滑鼠移到 bounds 再點擊：拒絕，因為這會直接削弱 JavaFX introspection 的核心價值

### 5. 將 discovery snapshot 與 action command 分離

Node enumeration 回傳的 snapshot 包含穩定欄位，例如：
- runtime node handle/reference
- `fx:id`
- node type
- text content
- hierarchy path
- visibility 與 enabled state

Action command 則根據 staleness policy 對 fresh 或 cached snapshot 做 resolve。

理由：
- 讓 `get_nodes()` 保持簡單且容易測試
- 避免把 discovery latency 耦合到每一次 action
- 有利於未來 recorder 與 debug tooling

備選方案：
- 每個 action 都重跑整棵 scene graph：雖然簡單，但在 UI 快速變動下成本較高，也較難分析行為

### 6. Recorder-lite 只輸出受支援 click case 的 script line

Recorder-lite 會捕捉本機 click event，並嘗試映射成：
1. JavaFX selector by `fx:id`
2. JavaFX selector by `type + text`
3. 若 structural mapping 失敗，則可選擇輸出 OCR text click expression

若沒有穩定 mapping，recorder 不會輸出誤導性的 script line。

理由：
- 避免產生脆弱腳本
- 符合 Phase 1 對 recorder scope 的限制

備選方案：
- 直接錄 raw coordinate：拒絕，因為這違反 framework 的可靠性目標

## Risks / Trade-offs

- [Java agent attachment 在不同 launch mode 與 JVM configuration 下可能有差異] -> Mitigation: 先定義 Phase 1 support matrix，並以 reference JavaFX app 驗證
- [JavaFX node identity 在 scene 更新後可能 stale] -> Mitigation: 使用可 refresh 的 snapshot，必要時在 action 前 re-resolve
- [OCR false positive 可能點到錯誤 control] -> Mitigation: 納入 confidence threshold、bounding box preview 與 deterministic fallback order
- [Template matching 對 theme 與 scaling 變化敏感] -> Mitigation: 將 vision 保持為最後 fallback，記錄 confidence，並將 Phase 1 demo 限縮在可控資產上
- [HTTP JSON 的契約鬆散度高於 gRPC] -> Mitigation: 在 code 中定義清楚的 request / response schema，並加入 Python / Java contract test
- [Recorder-lite 覆蓋率若被誤解為 full recorder，可能造成落差] -> Mitigation: 文件中明確標示 Phase 1 只支援 selector-stable 的 click capture

## Migration Plan

這是一組新的 capability，因此沒有既有 framework 的 production migration。Rollout 計畫如下：

1. 建立一個提供 login flow 與代表性 control 的 reference JavaFX sample app
2. 對 sample app 實作 Java agent attach 與 node enumeration
3. 加入 Python client connectivity 與 direct JavaFX action
4. 將 OCR / vision fallback service 加入同一條 action pipeline
5. 補上 observability 與 demo assertion
6. 在 core action path 穩定後再加入 recorder-lite

Rollback strategy:
- 若 fallback service 影響 MVP 穩定性，可先只交付 JavaFX-only vertical slice，將 OCR / vision 放在 feature flag 後方，待 confidence threshold 驗證後再啟用

## Open Questions

- 開發期預設要採用哪一種 Java attach strategy：JVM startup agent、dynamic attach，或兩者都支援？
- 哪一個 OCR engine 能在 Phase 1 取得 setup simplicity 與多語準確率的最佳平衡？
- 哪個 template-matching library 最能兼顧 Python / Java packaging simplicity？
- `type()` 是否應預設輸入到目前 focus element，還是必須要求顯式 selector？
- `get_nodes()` 對外應暴露多少 node metadata，才能既有用又不會洩漏過多難以維持兼容的 JavaFX internal detail？
