## Context

目前的 OmniUI reference app 只覆蓋很窄的 JavaFX slice：text input、button click 與 status verification。這足以證明 agent 與 Python control path，但不足以暴露最容易讓 automation framework 出問題的 JavaFX control pattern，例如 virtualized controls、popup-based selectors、hierarchical views，以及無法只靠 raw click 表達的 selection model。

這個 change 是 demo-driven 的。目標不是立刻宣稱完整支援所有複雜 JavaFX control，而是建立具代表性的場景，強迫 Java agent、selector model 與 Python API 面對真實控制項行為，藉此辨識哪些互動仍可用 `click` / `type` 表達，哪些需要更高階 contract，如 selection 或 expansion。

目前限制：
- Java agent 與 Python client integration 已存在，這次仍沿用它作為基礎執行路徑。
- demo app 必須保持可讀、穩定、易於人工執行。
- 新場景要用來揭露 structural automation gap，而不是單純堆 UI。
- OCR / vision 目前仍是 placeholder，本次重點仍是 JavaFX structural interaction。

## Goals / Non-Goals

**Goals:**
- 新增代表性的 JavaFX demo 場景，涵蓋 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-like layout。
- 用這些場景驗證目前 JavaFX discovery 對 virtualized、popup-driven 與 hierarchical controls 的行為。
- 判斷哪些進階互動可用現有 action 表達，哪些需要新的 action contract。
- 讓 demo 同時適合 manual smoke 與 Python-driven validation。
- 新增可執行的 Python demo，直接展示 OmniUI 面對 richer controls 的行為。

**Non-Goals:**
- 不在這次 change 內完整支援所有 JavaFX control 或 skin 實作。
- 不涵蓋 drag-and-drop、inline cell editing、keyboard navigation 全覆蓋或第三方 custom controls。
- 不更換 OCR / vision placeholder backend。
- 不一次解完所有 virtualization 與 off-screen item 問題。
- 不擴張到 JavaFX 以外的 toolkit。

## Decisions

### 1. 進階控制項以明確場景呈現，而不是做成一個大雜燴畫面

demo app 應把進階控制項拆成可辨識的場景或 section，而不是全部塞在一個密集的大畫面裡。

建議分組：
- selection controls：`ComboBox`、`ListView`
- hierarchy controls：`TreeView`
- table/grid controls：`TableView`、grid-like layout

原因：
- 讓 humans 與 Python scripts 都更容易理解場景意圖。
- 問題更容易隔離與診斷。
- 避免 scene graph 過於雜亂，讓 discovery 輸出失去可讀性。

### 2. 將 selection-oriented interaction 視為新 action contract 的候選

設計上應假設某些進階控制項更適合用 `select`、`expand`、`collapse` 之類 action 表達，而不是只靠 `click`。

預期對應：
- `ComboBox`：open/select item，再驗證 selected value
- `ListView`：select item by text/index，再驗證 selection
- `TreeView`：expand/collapse/select tree item
- `TableView`：select row 或 cell，再讀取 visible value

原因：
- 這些控制項的本質是 selection state，而不只是 pointer activation。
- 全部硬塞成 `click` 會讓 script 脆弱，且語意不清。

### 3. 第一步先以 visible state 為主處理 virtualized controls

對 `ListView`、`TreeView`、`TableView`，第一版應只明確保證 visible / materialized UI state。

含義：
- discovery 目前只承諾可見的 cell/row/item
- Python demo 先用小資料集，確保預期項目不需 scroll 就可見
- 後續再處理 scroll-aware resolution 或 off-screen items

原因：
- JavaFX virtualized controls 會 recycle cell，不保證 off-screen content 有穩定 node。
- 先把 visible state 做穩，比假裝已解完整 virtualization 更實際。

### 4. 將 popup-backed controls 視為獨立 discovery case

像 `ComboBox` 這種控制項的 popup 可能不在 primary scene root 內，因此 discovery model 必須預期 interaction 過程中會出現額外 window 或 popup container。

原因：
- agent-side discovery 已朝 window-based runtime detection 方向發展。
- popup-backed controls 是驗證 discovery 是否能跨出 base scene root 的直接測試。

### 5. demo data 與 selector 要以可讀性與穩定性為優先

進階場景應使用小型、命名清楚、可讀的資料集。

例如：
- `ComboBox`：`Admin`、`Operator`、`Viewer`
- `ListView`：短而唯一的 item 名稱
- `TreeView`：明確的 parent-child label
- `TableView`：穩定 sample records 與可辨識 column

原因：
- discovery 輸出更容易閱讀
- selector 與 regression assertion 更簡單
- 問題比較不會被模糊資料掩蓋

### 6. 新增 scenario-specific Python demos，不污染現有 login flow

現有 login flow 保持不變，進階控制項另外新增 Python demos。

預期新增：
- advanced discovery demo
- selection demo
- tree/table demo
- 可能的 curated advanced smoke script

原因：
- 保留 Phase 1 既有 vertical slice 作為穩定 baseline
- 讓進階控制項驗證可以獨立演進
- 避免 `run_demo.py` 與文件變得難懂

## Risks / Trade-offs

- [Virtualized controls 可能只暴露 visible cells，造成 discovery 不完整] -> Mitigation: 第一版明確限定 visible state，並用小型穩定資料集。
- [Popup-backed controls 可能在目前 discovery path 下不可見] -> Mitigation: demo 設計要明確觸發 popup visibility，並依結果調整 discovery contract。
- [新增 action verbs 可能讓 Python API 擴張太快] -> Mitigation: 只為被 demo 證明必要的情境新增最少量高階 action。
- [複雜 demo screens 可能讓 discovery output 太吵] -> Mitigation: 以 section / scenario 拆分，並控制資料量。
- [進階 demos 可能比 implementation 更早暴露限制] -> Mitigation: 將這些場景視為 contract-discovery tool，而不是完整支援宣告。

## Migration Plan

1. 設計進階 demo app 的場景結構，選出代表性控制項。
2. 實作 JavaFX demo sections 或 screens，並加入穩定 IDs 與 sample data。
3. 對每個場景執行 discovery，觀察 Java agent 能看到什麼。
4. 判斷現有 action 是否足夠，若不足再定義新 contract。
5. 新增對應的 Python demo scripts。
6. 擴充 manual smoke 與 regression test 覆蓋新場景。

Rollback strategy：
- 若某個 control type 太不穩，可保留其他場景，將該 control 延後到下一個 change。
- 不要因為某一類 control 尚未成熟就阻擋整體 demo 擴充。

## Open Questions

- 進階控制項應該放在多 tab、section，還是 separate route/scene？
- `select(...)` 是否需要立刻加入，還是第一版先用 `click + verify` 即可？
- `TreeView` item identity 應該用純文字、階層文字路徑，還是更豐富的 selector shape？
- `TableView` 第一版應該聚焦 row、cell，還是兩者都支援？
