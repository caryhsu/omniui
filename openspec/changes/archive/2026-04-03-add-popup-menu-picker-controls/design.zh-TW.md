## Context

OmniUI JavaFX automation framework（`add-omniui-javafx-automation-framework`）已建立：
- Python client 與 Java agent 之間的 loopback HTTP JSON protocol
- 透過 `Platform.runLater` 在 JavaFX application thread 上進行 scene graph 列舉
- Priority-based selector resolution：`fx:id` → type+text → OCR → vision
- 對 `click`、`type`、`get_text` 的 direct node-level event dispatch

Popup-backed control——ContextMenu、MenuBar 選單、DatePicker、Dialog、Alert——會以獨立的 `Window` instance 出現在主要 `Stage` scene graph 之外。現有的 agent 只列舉主要 scene。這次 change 將擴充 agent 與 Python client，使其能 discover 並與 overlay window 互動。

從現有設計繼承的主要限制：
- Java agent 僅透過 loopback HTTP 通訊
- 所有 JavaFX scene 存取都在 JavaFX application thread 上進行
- JavaFX-resolvable interaction 不使用 OS-level pointer movement
- Python client API surface 必須維持向後相容

## Goals / Non-Goals

**Goals:**
- 擴充 Java agent，列舉所有開啟中 `Window` instance 的 node，包含 `PopupWindow` 與 Dialog `Stage`
- 實作 wait-for-overlay：觸發 popup 動作後，輪詢直到預期的 overlay window 出現
- 透過逐步 hover dispatch，支援 ContextMenu 與 MenuBar 的多層選單遍歷
- 支援 DatePicker calendar popup 導航（月份）與以 `LocalDate` 值選取日期格
- 支援 Dialog 與 Alert 偵測、內容讀取，以及按鈕互動
- 新增各 control 專屬的 Python client method：`right_click`、`open_menu`、`navigate_menu`、`pick_date`、`get_dialog`、`dismiss_dialog`
- 擴充 demo app，為五種新 control 各加入專屬 scene

**Non-Goals:**
- 不更動 transport protocol——HTTP JSON 維持不變
- 不新增 selector resolution tier
- 不支援 detached 或 remote overlay window（headless、cross-JVM）
- 不自動化與 OS-native file chooser dialog 的互動
- 不支援 DatePicker 內的 drag interaction

## Decisions

### 1. Overlay node 透過 `Window.getWindows()` 列舉

JavaFX 的 `Window.getWindows()` 回傳所有開啟中的 window，包含 `PopupWindow`（ContextMenu、MenuBar popup、DatePicker popup）與 `Stage`（Dialog、Alert）。觸發 popup 動作後，agent 輪詢此清單，直到預期的 overlay window 出現。

理由：
- 不需要 hook application code 或監聽 window-creation event
- 對所有 popup-backed control 類型提供一致的作法
- 是現有 `Platform.runLater` agent model 的自然延伸

備選方案：
- 監聽 `WindowEvent.WINDOW_SHOWN`：需要 event hookup，與現有 agent 結構不一致
- 輪詢主要 scene graph 子節點尋找 popup node：無法覆蓋 separate `Window` instance

### 2. Wait-for-overlay 使用短輪詢加可設定 timeout

觸發 popup 動作後，agent 在 JavaFX thread 上以短間隔（預設 50 ms）輪詢 `Window.getWindows()`，直到符合條件的 overlay window 出現，或超過 timeout（預設 2 秒）。

理由：
- JavaFX popup rendering 是非同步的，沒有同步 API 可等待它
- 短輪詢避免 busy-wait 的 CPU 浪費
- 可設定的 timeout 讓測試在不同速度的環境下都能可靠運作

備選方案：
- 固定 sleep：不確定且在快速機器上浪費時間
- `AnimationTimer`：比 polling loop 複雜，好處有限

### 3. 多層選單遍歷使用逐步 hover dispatch

對多層 ContextMenu 與 MenuBar submenu，Python client 傳入一個 path（例如 `["File", "Export", "CSV"]`）。Agent 對每個中間 item 發送 mouse-entered event（hover），並等待其 submenu window 出現在 `Window.getWindows()` 後，再繼續下一層。

理由：
- 符合現有「event-level JavaFX interaction 優先於 OS 座標」的設計原則
- JavaFX submenu 在 hover event 下能可靠地展開，不需要 OS 滑鼠移動
- Path-based API 對 automation script 直覺易讀

備選方案：
- OS 滑鼠移動：違反設計原則，跨 DPI 不穩定
- 透過 reflection 觸發 submenu show：與 JavaFX 內部耦合過深

### 4. DatePicker 日期格透過 `LocalDate` item property 識別

DatePicker popup 的日期格（`DateCell`）透過 `item` property 暴露其 `LocalDate` 值。Agent 列舉 `DatePickerContent` 下的 cell，根據 `LocalDate` 值匹配目標格，再 dispatch click event。月份導航透過對 forward/backward navigation button 的 click dispatch 實作。

理由：
- `LocalDate` 識別方式穩定，不受 locale 影響
- 比比對顯示文字更能抵抗 locale 與月份排列差異

備選方案：
- 以 cell text（例如 "15"）識別：跨 locale 與月份排列會有 ambiguity
- 以 grid position 識別：月份起始日不同時會偏移

### 5. Dialog 與 Alert 透過 `DialogPane` root type 識別

Dialog 與 Alert 以 separate `Stage` 呈現，其 scene root 為 `DialogPane`。Agent 掃描 `Window.getWindows()` 找到 root 為 `DialogPane` 的 `Stage`，再讀取其 header text、content text 與 `ButtonBar` 中的 button。

理由：
- `DialogPane` 是 JavaFX 標準的 dialog root；偵測不需要 application code
- 從 button 讀取 `ButtonData` 讓 Python client 能以語意名稱（OK、CANCEL）識別按鈕，不依賴 locale text

備選方案：
- 以 window title 識別 dialog：脆弱，application 可能自訂 title
- 直接識別 `ButtonType`：`ButtonData` 比 button 顯示文字更穩定

### 6. 每種 control type 使用獨立的 Python API method

不使用泛用的「click overlay item」API，而是暴露五個專屬 method：`right_click`、`open_menu`、`navigate_menu`、`pick_date`、`get_dialog`、`dismiss_dialog`。

理由：
- 在 automation script 中清楚表達操作意圖
- 每個 method 有明確的 parameter 契約與錯誤訊息
- 每種 control type 可有獨立的 action trace 與 observability

備選方案：
- 泛用 `interact_overlay(type, action, ...)`：隱藏 control type 差異，debug 更困難

## Risks / Trade-offs

- [JavaFX popup 渲染時機在不同機器與 JVM 版本間不一致] → Mitigation: wait-for-overlay 的 timeout 與 polling interval 可在每次呼叫時設定
- [多層 submenu hover 後出現的時機不固定] → Mitigation: 每次 hover 都等待 submenu window 出現後才繼續，不使用固定 sleep
- [DatePicker 內部 node 結構（`DatePickerContent`、`DateCell`）可能在不同 JavaFX 版本不同] → Mitigation: 建立受測 JavaFX 版本 support matrix，並在 demo 中加入版本 assertion
- [Dialog 偵測假設使用標準 `javafx.scene.control.Dialog`；自訂 dialog 可能無法偵測] → Mitigation: Phase 1 範圍明確限制於標準 `Dialog` 與 `Alert`，並在文件中標示此限制
- [`Window.getWindows()` 可能包含非 popup window，需過濾] → Mitigation: 以 window 類型與 scene root 類型雙重過濾，降低誤判

## Migration Plan

這次 change 對現有 selector 與 action 沒有 breaking change，是純粹的能力擴充。Rollout 順序：

1. 擴充 Java agent，加入 `Window.getWindows()` 列舉與 wait-for-overlay 輪詢機制
2. 加入 ContextMenu 觸發、item 選取與多層遍歷
3. 加入 MenuBar menu 啟動與多層 submenu 遍歷
4. 加入 DatePicker popup 開啟、月份導航與日期選取
5. 加入 Dialog 偵測、內容讀取與按鈕互動
6. 加入 Alert 偵測、訊息讀取與按鈕互動
7. 擴充 demo app 加入各 control 的 scene；新增 Python demo script
8. 更新 Python client 加入新 API method

Rollback strategy:
- 若 overlay 列舉影響現有 action 穩定性，可透過 feature flag 控制 overlay window scan 的啟用，確保主要 scene 行為不受影響

## Open Questions

- Wait-for-overlay 的預設 timeout 應設多少，才能兼顧 CI 速度與慢速機器的可靠性？
- `navigate_menu(path=[...])` 的每一層 identifier 是否應同時支援 text 與 fx:id？
- `pick_date` 是否應自動跨越多個月份導航，若目標日期相距很遠？
- Dialog button 匹配應優先使用 `ButtonData` 語意類型、以 text 作為 fallback，還是讓呼叫端明確選擇匹配策略？
- 若 Alert 含有 expanded text，是否應一併納入 `get_dialog()` 的回應？
