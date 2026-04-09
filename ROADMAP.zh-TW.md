# OmniUI 開發路線圖

> English version: [ROADMAP.md](ROADMAP.md)

本文件追蹤計畫中的功能與改善項目。完成並 archive 後請勾選對應項目。

---

## ✅ 已完成

- 登入流程自動化（`click`、`type`、`verify_text`）
- ComboBox、ListView、TreeView、TableView 選取
- ContextMenu、MenuBar
- DatePicker（`open_datepicker`、`set_date`、`pick_date`、`navigate_month`）
- Dialog / Alert（`get_dialog`、`dismiss_dialog`）
- RadioButton / ToggleButton（`get_selected`、`set_selected`）
- Slider / Spinner（`set_slider`、`set_spinner`、`step_spinner`）
- ProgressBar / ProgressIndicator（`get_progress`、`get_value`）
- TabPane（`get_tabs`、`select_tab`）
- TextArea、PasswordField
- Hyperlink（`is_visited`）
- CheckBox、ChoiceBox
- Accordion / TitledPane（`expand_pane`、`collapse_pane`、`get_expanded`）
- TreeTableView（選取列、讀取儲存格、展開／收合）
- ColorPicker（`set_color`、`get_color`、`open_colorpicker`、`dismiss_colorpicker`）
- SplitPane（`get_divider_positions`、`set_divider_position`）

---

## 🔥 高優先 — 常見測試痛點

- [x] **捲動控制** — `scroll_to(id=...)`、`scroll_by(delta_x, delta_y, id=...)`
  - 支援 ScrollPane；`scroll_to` 向上走 parent chain；`scroll_by` 使用 0–1 正規化偏移量
- [x] **鍵盤快捷鍵** — `press_key("Ctrl+C")`、`press_key("Enter")`、`press_key("Escape")`
  - 補完鍵盤操作缺口，對 Dialog / TextField 流程很重要
- [x] **雙擊** — `double_click(id=...)`
  - TreeView / TableView 展開節點的常用操作
- [x] **關閉應用程式** — `close_app()`
  - 從 agent 內呼叫 `Platform.exit()`；適合測試收尾與關閉行為驗證
- [x] **等待條件（waitFor）** — `wait_for_text(id, expected, timeout)`、`wait_for_visible(id, timeout)`、`wait_for_enabled(id, timeout)`、`wait_for_node(id, timeout)`、`wait_for_value(id, expected, timeout)`
  - Python 端輪詢或 agent 端阻塞；非同步 UI 狀態變更不可或缺
- [x] **App 啟動 API** — `launch_app(jar=..., port=...)` 從 Python 直接啟動含 agent 的 JavaFX app
  - 目前每次測試前需手動啟動 agent；補齊與 Playwright / WinAppDriver 的落差

---

## 🧩 中等優先 — 補充控制項

- [x] **節點狀態查詢** — `is_visible(id=...)`、`is_enabled(id=...)`
  - Agent 的節點探索已包含 `visible` / `enabled`；只需在 Python 端加上便利封裝
- [x] **Tooltip 驗證** — `get_tooltip(id=...)`
  - 讀取節點的 Tooltip 文字
- [x] **CSS 樣式檢查** — `get_style(id=...)`、`get_style_class(id=...)`
  - `get_style()` 回傳節點的 inline style 字串（例如 `"-fx-text-fill: red;"`）
  - `get_style_class()` 回傳節點套用的 CSS class 名稱清單
  - 可用於驗證狀態測試：紅色/綠色、error/success CSS class，不依賴特定錯誤文字
- [x] **多選（Multi-select）** — `select_multiple(id=..., values=[...])`
  - ListView / TableView 多項目選取；`get_selected_items(id=...)` 以清單回傳目前所有已選項目
- [x] **Modifier+click** — `click(id=..., modifiers=["Ctrl"])`、`click(id=..., modifiers=["Ctrl", "Shift"])`
  - Ctrl+click 加選、Shift+click 範圍選取；適用於 ListView/TableView 多選工作流，不需要另呼叫 `select_multiple`
- [x] **TableView 儲存格編輯** — `edit_cell(id=..., row=..., column=..., value=...)`
  - 雙擊儲存格並輸入新值（需要 editable TableView）
- [x] **TableView 欄位排序** — `sort_column(id=..., column=..., direction="asc")`
  - 點擊欄位標題觸發排序並讀回結果
- [x] **index= selector** — `click(type="Button", index=0)`、`click(id="myList", index=2)`
  - 無唯一 id 時選取第 N 個符合節點；解決動態/自動產生節點（ListView cell、重複控制項）的選取問題
- [x] **TableView 位置存取** — `get_cell(id=..., row=N, column=N)`、`click_cell(id=..., row=N, column=N)`
  - 用列/欄索引存取特定儲存格，不依賴 value 比對；適用於無唯一 id 的動態表格內容
- [x] **ToolBar** — `get_toolbar_items(id=...)`，透過現有 `click` 操作工具列按鈕
  - 存取與操作 `ToolBar` 容器中的項目
- [x] **ScrollBar** — `get_scroll_position(id=...)`、`set_scroll_position(id=..., value=...)`
  - 對獨立 `ScrollBar` 節點進行精細捲動控制
- [x] **Pagination（分頁）** — `get_page()`、`set_page(n)`、`next_page()`、`prev_page()`
  - 自動化 JavaFX `Pagination` 控件
- [x] **Window / Stage 管理** — `get_windows()`、`focus_window(title=...)`
  - 多視窗測試場景
  - `maximize_window()`、`minimize_window()`、`restore_window()`
  - `set_window_size(width, height)`、`set_window_position(x, y)`
  - `is_maximized()`、`is_minimized()`、`get_window_size()`、`get_window_position()`
  - 透過 JavaFX `Stage` reflective call 實作（`setMaximized`、`setIconified`、`setX/Y` 等）
- [x] **絕對座標點擊** — `click_at(x=100, y=200)`
  - Canvas 自繪或無 scene graph 節點的 UI 元素的 fallback 操作方式
- [x] **範圍限定選擇器（`within`）** — `with client.within(id="panel"): client.click(id="btn")`
  - 將節點搜尋限定在子樹範圍；避免多個 panel 共用相同子節點 ID 時發生衝突
- [x] **Scene graph 快照** — `client.snapshot()` 擷取完整 UI 節點狀態為結構化 list
- [x] **Scene graph 差異比對** — `client.diff(before, after)` 比較兩個快照，回傳新增/移除/變更的節點
  - 比 `verify_text` 更全面；適合驗證單一操作的所有副作用

---

## 🏗️ 基礎建設 / 開發體驗

- [x] **`verify_text` 彈性比對** — 支援 `match="contains"`、`match="starts_with"`、`match="regex"` 模式
  - 目前只有 exact match，常常過於嚴格
- [x] **Locator 物件** — `loc = client.locator(id="btn")` 回傳可重複使用的元素 handle；可呼叫 `loc.click()`、`loc.verify_text(...)`、`loc.wait_for_visible()` 而不需重複寫 selector
  - 支援 Page Object Model，減少 selector 重複
- [x] **Page Object Model 基礎類別** — `OmniPage` base class 自動注入 `client`；給測試專案標準化結構
- [x] **Soft Assertions** — `with client.soft_assert() as sa:` 收集所有失敗後一次回報，不會第一個就中斷
- [x] **Retry 輔助器** — `@client.retry(times=3, delay=0.5)` 裝飾器，用於不穩定的 assertion 區塊
- [x] **結構化動作 Trace** — 每個操作自動記錄 timestamp、selector、結果；測試失敗時輸出完整動作時間軸
- [x] **平行測試支援** — `pytest-xdist` 整合；`_worker_port()` helper；`omniui_parallel` session fixture；`conftest_parallel_example.py`；`docs/parallel-testing.zh-TW.md`
- [x] **pytest fixture 整合** — `@pytest.fixture` 自動 connect/disconnect，減少測試樣板程式碼
- [x] **失敗自動截圖** — 任何操作拋出例外時自動擷取並儲存截圖
- [x] **自訂 wait 條件** — `wait_until(fn, timeout)` 接受自訂 lambda，支援任意輪詢邏輯
- [x] **Headless 模式** — Linux 使用 Xvfb 虛擬顯示器；`run_all.py` 自動偵測 OS 並選擇對應的 JavaFX JAR；詳見 `docs/headless.zh-TW.md`
- [x] **CI/CD 範例** — GitHub Actions workflow：`ci-unit.yml`（僅 pytest）與 `ci-integration.yml`（Xvfb + 完整 demo 測試）
- [x] **HTML 測試報告** — pytest-html 整合；失敗時截圖自動嵌入報告；詳見 `docs/html-report.zh-TW.md`
- [ ] **TableView 互動 API** — `click_row(id=..., row=0)` 選取或點擊特定資料列；`get_cell_text(id=..., row=0, column=1)` 讀取儲存格內容；`get_row_count(id=...)` 查詢結果筆數。目前點擊 TableView 資料列儲存格無法產生穩定 selector（儲存格沒有 `fx:id`），錄製時會誤捕為欄位標題點擊。
- [ ] **TreeView 互動 API** — `select_tree_item(id=..., path=["Documents", "Projects"])` 依路徑選取節點；`get_tree_items(id=...)` 列出所有可見節點。目前點擊 TreeView 節點因 `TreeCell` 沒有 `fx:id`，會被錄製成 `click(type="Group", index=0)`（fragile）。修復方向：Java agent 偵測 `TreeCell` click → 向上追蹤路徑到根節點 → 產出 `tree_select` 事件；codegen 生成 `select_tree_item`；Python engine 實作對應 API。
- [ ] **錄影功能** — 補充 screenshot 以外的偵錯工具
- [x] **Self-healing selector** — 當 locator 因 `fx:id` 被改名或移除而失敗時，自動依序嘗試快取的 `text`、`type+index` 作為備援；action trace 會記錄實際使用的備援方式，提示開發者更新 selector。
- [x] **拖放（Drag & Drop）** — `drag(id=...).to(id=...)` / `drag_to(id=..., to_x=..., to_y=...)`；觸發 MOUSE_PRESSED → MOUSE_DRAGGED × 5 → MOUSE_RELEASED
- [ ] **獨立 drag-drop demo app** — 獨立 JavaFX app，左右兩個 ListView 可拖曳項目（左 → 右）；作為更完整的 drag & drop 展示，取代塞在 advanced-app 中的簡陋區塊
- [x] **獨立 drag-drop demo app** — `demo/java/drag-app/` 獨立 app；左欄（Available）→ 右欄（Selected）逐項拖曳；`demo/python/drag/drag_listview_demo.py`
- [x] **Hover（懸停）** — `hover(id=...)` 觸發 Tooltip 或 hover 狀態
- [x] **剪貼簿操作** — `copy()`、`paste()`、`get_clipboard()`
- [ ] **升級 JavaFX 至 24+** — 目前 demo app 使用 JavaFX 21.0.2，**不支援**內建 Headless Platform（`-Dglass.platform=Headless`）。升級至 JavaFX 24+ 可在所有平台（Windows/Linux/macOS）實現真正的 headless 執行，不需要 Xvfb 或 Monocle。影響範圍：所有 demo `pom.xml` 與 `run_all.py _JAVAFX_VERSION`。`run_all.py --headless` 旗標已實作但受此升級阻擋。

---

## 🎬 完整錄製器（Full Recorder）

- [x] **事件捕捉** — Java agent 在 Scene 上掛 `EventFilter`，攔截滑鼠點擊、雙擊、按鍵與文字輸入
- [x] **Selector 推導** — 從被點擊的節點自動選出最佳 selector：`fx:id` → `text` → `type + index`
- [x] **腳本生成** — Python 端將錄製事件序列化為可執行的測試腳本
  - 輸出 `click`、`type`、`press_key`、`verify_text` 等指令
  - 敏感欄位（如 `PasswordField`）自動遮罩，替換為佔位符
- [x] **Wait 自動插入** — 啟發式在每個 action 之間插入 `wait_for_*`；`start_recording(wait_injection=True)`
  - Button/ComboBox/CheckBox/... → `wait_for_enabled`；其他節點 → `wait_for_visible`
  - 無法穩定表達的 selector（無 `fx:id` / `text`）會略過
- [x] **錄製 Session API** — `start_recording(wait_injection)` / `stop_recording()` / `save_script(path)`
- [ ] **錄製 UI 工具** — ~~互動式 TUI/GUI 應用程式（`python -m omniui.recorder`），提供錄製 / 停止 / 儲存控制，讓非工程師也能不寫程式即可錄製操作~~
- [x] **錄製 UI 工具** — `python -m omniui.recorder` tkinter GUI；自動掃描執行中的 app，提供錄製 / 停止 / 儲存流程
  - [x] **Wait 插入 checkbox** — GUI 中的「Insert wait_for_*」開關；錄製開始時套用設定
- [ ] **拖放錄製（Drag & Drop recording）** — Recorder 捕捉 `MOUSE_PRESSED` + `MOUSE_RELEASED` 配對，推導起終點節點，在產出腳本中輸出 `client.drag(id=...).to(id=...)`
- [x] **拖放錄製（Drag & Drop recording）** — `MOUSE_PRESSED`+`MOUSE_RELEASED` 距離 ≥15px 即記錄 drag 事件；codegen 輸出 `client.drag(id=...).to(id=...)`
- [x] **即時錄製回饋（Real-time recording feedback）** — 目前 Recorder GUI 需等到按下 Stop 後才會顯示錄製腳本。計畫加入 polling 機制（每 ~500ms 呼叫 `GET /sessions/{sessionId}/events/pending`），讓每個使用者操作在錄製時立即顯示於腳本預覽區。升級路徑：先實作 polling，若需要低於 100ms 延遲或支援瀏覽器 UI 再改用 SSE。
- [x] **錄製後立即執行（Record & Run）** — Recorder GUI 加入 Run All 和 Run Selection 按鈕；透過 `exec()` 對連接的 agent 執行錄製腳本
- [ ] **錄製時加入 Assertion** — 錄製過程中可對任意元素按右鍵，插入 `verify_text` / `verify_visible` / `verify_enabled` 驗證步驟；產出腳本中驗證步驟與操作步驟並列。
- [ ] **Recorder 步驟編輯** — 停止錄製後，可在腳本預覽區刪除或重新排序個別步驟再儲存；支援拖拉排序或選取後刪除不需要的步驟。
- [ ] **失敗時自動截圖** — 腳本步驟發生例外時自動擷取截圖並附加至錯誤輸出；與 Record & Run 及 CI 報告整合。
- [ ] **Locator Inspector** — 點擊執行中 app 的任意元素，顯示所有可用 selector（`fx:id`、`text`、`type+index`）並依穩定性排序；讓開發者無需寫程式即可確認或複製最佳 selector。
- [ ] **System Tray（Windows/macOS/Linux）** — 關閉 Recorder 視窗時改為縮到系統通知區（Windows 右下角、macOS 選單列、Linux tray），而非直接結束；右鍵選單：Show / Hide / Quit。需要 `pystray` + `Pillow`；圖示以程式碼動態產生，不需額外圖檔。

### 已知錄製器問題（Known Recorder Issues）

- [x] **Dialog 內部脆弱 selector** — 點擊 dialog 內部的 layout 節點（如 spacing `Pane`）時，因無 `fx:id` 或穩定文字，會被錄製為 `click(type="Pane", index=0)` 並標注 `# WARN: fragile selector`，產出腳本不穩定。修復：錄製時略過非互動性的 layout 節點，並直接對 ButtonBar 內的按鈕附加 ACTION handler，使 `dismiss_dialog` 能正確錄製。*(已 merge PR #19)*

---

## 🧪 測試基礎設施（Testing Infrastructure）

- [ ] **e2e demo scripts 未被 pytest 收錄** — `demo/python/*/` 下的 demo scripts 是真正的 end-to-end 測試，會啟動 app 並驗證完整流程，但沒有被 `pytest tests/` 收錄。目前只有 `test_parallel_example.py` 透過 `OmniUI.launch()` 對真實 app 進行整合測試，其餘 `tests/` 都是 Python 單元測試（mock）。修復方向：將每個 demo script 包裝成帶有 `@pytest.mark.integration` 的 pytest 整合測試，並在 CI 加上 `--integration` flag 讓完整 e2e suite 可以獨立選擇執行。

---

## 💡 構想 / 未來規劃

- [x] **login-app 從 core-app 拆分** — `core-app` 目前將登入 Demo 與其他核心控制元件（ListView、TableView、ComboBox 等）混在一起。將登入表單獨立為 `demo/java/login-app`，使 `core-app` 專注於控制元件覆蓋範圍，`login-app` 則成為認證流程自動化（`click`、`type`、`verify_text`）的獨立範例。 ✅
- [ ] **Mini Explorer demo app** — 假檔案瀏覽器（`demo/java/explorer-app`），用於測試 TreeView 導覽與水平 SplitPane 分隔調整。佈局：左側 = TreeView（模擬資料夾/檔案層級）；右側 = 選取資料夾後顯示對應檔案清單；使用者可拖曳調整分隔線。覆蓋：`select_tree_item`、`get_tree_items`、`get_divider_positions`、`set_divider_position`。
- [ ] **Settings Panel demo app** — 設定表單 App（`demo/java/settings-app`），用於測試多種輸入控制元件的綜合場景。建議控制元件：TextField、PasswordField、ComboBox、CheckBox、RadioButton、Slider、Spinner、ToggleButton，加上 Save / Reset 按鈕。可單頁展示或用 TabPane 分區（如 Account / Appearance / Notifications）。
- [ ] **Layout Playground demo app** — `demo/java/layout-app` 互動式 Layout 展示工具，用於視覺化比較所有 JavaFX 主要布局管理器的行為差異。UI 架構：BorderPane 外框，頂部 ToolBar 含 ComboBox `layoutSelector`（HBox / VBox / GridPane / BorderPane / FlowPane / TilePane / StackPane）、`resetBtn`、`randomizeBtn`；左側 Control Panel 根據所選 Layout 動態顯示對應的 Slider / Spinner / CheckBox（spacing、padding、alignment、gap、gridLines、orientation、wrap-length、tile columns、子節點順序等）；中央 Preview Area 即時渲染當前 Layout，內含 5–8 個標籤節點（不同背景色 + 紅色邊框，方便辨識）；底部 StatusBar 顯示目前 Layout 名稱、子節點數量與預覽尺寸。進階功能：視窗縮放測試（FlowPane / TilePane 自動換行）、新增 / 移除節點、顯示 layout 邊界。完整覆蓋所有 JavaFX Layout 類別，以及 Slider、Spinner、ComboBox、CheckBox、Button、Rectangle。
- [ ] **Notepad demo app** — `demo/java/notepad-app` 輕量級多分頁文字編輯器，規格仿照 Notepad++。UI：BorderPane 架構，頂部 MenuBar + ToolBar，中央 TabPane（`editorTabPane`），底部 StatusBar。MenuBar：File（New、Open、Save、Save As、Exit）、Edit（Undo、Redo、Cut、Copy、Paste、Delete、Select All）、View（CheckBoxMenuItem：Word Wrap / Show Line Numbers / Status Bar）、Help（About）。ToolBar：New / Open / Save / Cut / Copy / Paste 按鈕。每個 Tab 包裝 TextArea（BorderPane 內）；文件有未儲存變更時 tab 標題顯示 `*`。StatusBar：檔案路徑、游標位置（`Ln N, Col N`）、編碼資訊。檔案操作使用 FileChooser；關閉有修改的 Tab 顯示 Alert（Save / Discard / Cancel）。進階功能：Find/Replace Dialog（TextField + 高亮）、字體選擇器（ComboBox + Slider）、拖拉開啟檔案、Ctrl+S/O/N/F 快捷鍵。覆蓋元件：MenuBar、Menu、MenuItem、CheckBoxMenuItem、SeparatorMenuItem、Button、TextArea、TabPane、Label、FileChooser、Alert、Clipboard。
- [ ] **Analytics Dashboard demo app** — `demo/java/analytics-dashboard` 互動式即時資料視覺化平台。UI：BorderPane 架構，頂部 ToolBar 含 `datasetSelector` ComboBox（切換 Sales/Users/Traffic）、`timeRangeSelector` ComboBox（Last 24h/7d/30d）、`realTimeToggle` ToggleButton、`refreshBtn`、`exportBtn`；左側 Filter Panel（各分類 CheckBox、起訖日期 DatePicker 對、資料密度 Slider）；中央 2×2 GridPane 儀表板（LineChart 趨勢、BarChart 分類比較、PieChart 佔比、AreaChart 累積值）；底部 StatusBar（資料筆數 + 最後更新時間）。資料模型：`DataPoint`（timestamp、category、value）與 `DataSet`（name、list），以 `Timeline` 模擬即時更新。互動行為：資料點 Hover Tooltip、PieChart 點擊 slice 過濾其他圖表、各圖表跨欄 category 同步。匯出：`WritableImage` + `snapshot()` 輸出 PNG。覆蓋元件：LineChart、BarChart、PieChart、AreaChart、NumberAxis、CategoryAxis、ComboBox、ToggleButton、CheckBox、DatePicker、Slider、Timeline、Tooltip、Snapshot。
- [ ] **Browser demo app** — `demo/java/browser-app` 以 JavaFX WebView/WebEngine 實作的迷你瀏覽器。UI：BorderPane 架構，頂部導覽列（Back/Forward/Refresh `Button`、URL `TextField`（`urlBar`）、Go `Button`、載入中 `ProgressBar`），中央 `WebView`（`webView`），底部 StatusBar（頁面標題 Label、載入狀態 Label）。行為：按 Enter 或 Go 導覽；Back/Forward 綁定 `WebHistory`；`ProgressBar` 由 `WebEngine.loadWorker.progressProperty` 驅動；狀態 Label 隨 Worker state（SUCCEEDED/FAILED/CANCELLED）更新；頁面標題綁定 `WebEngine.titleProperty`。進階功能：`WebEngine.executeScript()` 示範（注入 JS、讀取 DOM 值）、右鍵選單「View Source」、書籤側邊欄（已儲存 URL 的 ListView）。需要 `javafx.web` 模組。覆蓋元件：WebView、WebEngine、TextField、Button、ProgressBar、ListView、ContextMenu、WebHistory。
- [ ] **Animation Playground demo app** — `demo/java/animation-app` JavaFX 動畫原語互動展示台。UI：BorderPane 架構，左側控制面板 + 中央舞台區域。每種動畫類型各有一張卡片，含目標節點（彩色 Rectangle 或 Label）、觸發 Button、以及參數列（duration Slider、循環 ComboBox、auto-reverse CheckBox）。動畫覆蓋：`FadeTransition`（透明度 1→0→1）、`TranslateTransition`（x/y 位移）、`RotateTransition`（0→360°）、`ScaleTransition`（1×→2×→1×）、`Timeline` + `KeyFrame`（自訂屬性插值：顏色漸變、同步多屬性、乒乓循環）。"Play All" Button 以 `ParallelTransition` 同時播放所有動畫；"Sequence" Button 以 `SequentialTransition` 串聯執行。StatusBar 顯示當前動畫名稱與狀態（RUNNING/PAUSED/STOPPED）。覆蓋元件：FadeTransition、TranslateTransition、RotateTransition、ScaleTransition、Timeline、KeyFrame、ParallelTransition、SequentialTransition、Slider、ComboBox、CheckBox、Button。
- [ ] **Drawing App demo app** — `demo/java/drawing-app` 自由繪圖與圖形繪製工具。UI：BorderPane 架構，頂部 ToolBar（工具選擇：Pen / Rectangle / Circle / Line / Polygon / Eraser；顏色 `ColorPicker`；筆寬 `Slider`；Clear 與 Undo `Button`），中央 `Canvas`（`drawingCanvas`）。自由繪圖模式以 `GraphicsContext.beginPath` / `lineTo` / `stroke` 回應滑鼠拖曳事件。圖形模式在拖曳時預覽、放開時定稿。圖形覆蓋：`Rectangle`、`Circle`（`drawOval`）、`Line`、`Polygon`（點擊加頂點，雙擊封閉）。Undo 以 `Stack<WritableImage>` 快照實作。進階功能：填色切換（CheckBox）、透明度 Slider、匯出畫布為 PNG（`WritableImage` + `ImageIO`）。覆蓋元件：Canvas、GraphicsContext、ColorPicker、Slider、Button、CheckBox、所有 JavaFX Shape 繪圖原語。
- [ ] **Inventory Manager demo app** — `demo/java/inventory-app` 產品庫存表，專門展示可編輯 TableView。UI：BorderPane 架構，頂部 Bar（搜尋 `TextField` + `FilteredList` 即時過濾、Add Row / Delete Row / Export CSV `Button`），中央可編輯 `TableView<Product>`（`inventoryTable`），底部 StatusBar（列數 + 選取行資訊）。欄位與 Cell Factory：Name（`TextFieldTableCell`）、Category（`ComboBoxTableCell`）、Price（`TextFieldTableCell` + `Double` converter）、Quantity（可編輯 `TextFieldTableCell`）、In Stock（`CheckBoxTableCell`）。每欄以 `SimpleStringProperty` / `SimpleDoubleProperty` / `SimpleBooleanProperty` 作為 `setCellValueFactory` 後端。`onEditCommit` 驗證輸入（名稱非空、price ≥ 0）並顯示行內錯誤樣式。Add Row 新增空白 `Product`；Delete Row 以 Alert 確認後刪除。所有欄位支援排序。覆蓋元件：TableView、TableColumn、TextFieldTableCell、ComboBoxTableCell、CheckBoxTableCell、FilteredList、SortedList、FileChooser、Alert、TextField、Button、Label。
- [ ] **Contact List demo app** — `demo/java/contact-list-app` 專門展示 `ListView` 自訂 `ListCell`。每個 Cell 為自訂 `HBox`：`StackPane` 頭像（彩色 `Circle` + 姓名縮寫 `Label`）、`VBox`（粗體姓名 + 灰色 email/電話 `Label`）、優先級 `Label` 徽章（CSS pill 樣式：VIP / Friend / Work）、以及 hover 時淡入的操作 `HBox`（Edit + Delete `Button`）。Cell factory 設定於 `listView.setCellFactory(...)`，回傳覆寫 `updateItem` 的 `ListCell<Contact>` 子類別。UI：BorderPane 架構，頂部 Bar（搜尋 `TextField` + `FilteredList`、Add `Button`、排序 `ComboBox`），中央 `ListView<Contact>`（`contactList`），底部 StatusBar。雙擊 Cell 開啟編輯 `Dialog<Contact>`（自訂 `DialogPane` + `GridPane` 表單）。右鍵選單：Edit、Delete、Mark as VIP。覆蓋元件：ListView、自訂 ListCell、Circle、FilteredList、Dialog、DialogPane、ContextMenu、TextField、ComboBox、Button、Label、CSS inline styling。
- [ ] **Org Chart demo app** — `demo/java/org-chart-app` 公司組織階層瀏覽器，專門展示 `TreeTableView`。資料模型：`Employee`（name、title、department、salary、status）以三層 `TreeItem` 組織（Company → Department → Team → Employee）。`TreeTableView<Employee>`（`orgTree`）欄位：Name（`TreeTableColumn`，含展開/收合箭頭）、Title、Department、Salary（`TextFieldTableCell` + `Double` converter，可編輯）、Status（`ComboBoxTableCell`：Active / On Leave / Inactive）。行為：點擊列展開/收合子節點；右鍵選單（Add Subordinate、Promote、Delete）；頂部 ToolBar（搜尋 `TextField` + `FilteredList` 展開符合節點、Expand All / Collapse All `Button`、Export CSV）；底部 StatusBar（員工總數 + 選取員工資訊）。進階：double-click 開啟員工詳細 `Dialog`；選取列高亮同一 Department 的所有列。覆蓋元件：TreeTableView、TreeTableColumn、TreeItem、TextFieldTableCell、ComboBoxTableCell、TreeItemPropertyValueFactory、ContextMenu、FilteredList、Dialog、Button、TextField。
- [ ] **Form Validator Lab** — `demo/java/fx-form-validator-lab` 表單與驗證實驗平台，覆蓋所有常見 JavaFX 表單元件。UI：BorderPane 架構，頂部 ToolBar（`formTypeSelector` ComboBox 切換 User/Payment/Settings 表單、`realtimeValidationCheck` CheckBox、`showTooltipCheck` CheckBox、`strictModeToggle` ToggleButton、`validateBtn`、`resetBtn`），左側 Form Section（GridPane 2欄排版，元件全集合：`TextField` username、`TextField` email、`PasswordField` password + confirm、`Spinner<Integer>` age、`ComboBox` gender、`DatePicker` birthday、`ChoiceBox` country、`CheckBox` subscribe、`TextArea` bio、`Slider` rating、`ColorPicker` theme colour），中央 Validation Preview（`Label` formStatusLabel、`ListView<String>` errorList、`TextArea` debugJson 即時顯示欄位狀態 JSON），底部 StatusBar。驗證規則：username ≥ 3 字元；email regex（strict mode 更嚴格）；password ≥ 8 字元含字母 + 數字；confirm 必須一致；age 1–120；birthday 不可為未來；rating ≥ 3（strict mode）。錯誤 UX：無效欄位紅框、欄位下方 inline 錯誤 `Label`、可選 hover `Tooltip`（由 `showTooltipCheck` 切換）。欄位狀態模型：每個欄位追蹤 `value`、`valid`、`touched`、`dirty`；表單層級 `FormState` 彙總 `valid`、`dirty`、`errors[]`。Realtime 模式在每次輸入/變更時驗證；手動模式只在按 `validateBtn` 時驗證。跨欄驗證：confirm password vs password；條件驗證：subscribe=true 時 email 必填。進階：模擬非同步 username 重複檢查（`PauseTransition` 延遲 1 秒 + 旋轉指示器）。Reset 清除所有欄位、錯誤與 dirty 狀態。覆蓋元件：TextField、PasswordField、TextArea、ComboBox、ChoiceBox、Spinner、DatePicker、Slider、ColorPicker、CheckBox、ToggleButton、ListView、Tooltip、PauseTransition。
- [ ] **多 App 自動化（Multi-app automation）** — 在單一測試流程中協調多個 JavaFX app
  - Python 端管理多條 agent 連線（每個 JavaFX app 各自嵌入一個 agent）
  - App 生命週期管理：啟動、連線、切換、斷線、關閉
  - App 識別方式：port、PID 或具名 alias
  - API 設計待定：`client.use("app1").click(...)` 或多個 `OmniUI` 實例，或兩者皆支援
  - 跨 App 工作流：例如在 app A 填寫表單，在 app B 驗證結果
- [ ] **VS Code Extension（Recorder）** — 提供側邊欄面板，內含錄製 / 停止 / 執行控制項的 VS Code 擴充功能。透過現有 HTTP API 與 Java Agent 溝通（不需修改 agent）。相較 Tkinter GUI 的關鍵優勢：可將錄製程式碼插入目前游標位置；Run Selection 直接對應 VS Code 原生的終端機執行功能；產出的腳本立即享有語法高亮與 IntelliSense。Tkinter GUI 與 VS Code Extension 可並存，extension 主要服務已在 VS Code 開發的工程師。
- [ ] **視覺回歸測試** — 截圖 baseline 比對，偵測非預期的 UI 變更
- [x] **焦點管理** — `tab_focus()`、`verify_focused(id=...)`
- [ ] **無障礙檢查** — 驗證 ARIA 角色與標籤
- [ ] **WebView** 自動化（執行 JavaScript）
- [ ] **效能指標** — 操作計時、畫面更新頻率

---

> **工作流程：** 挑一個項目 → `openspec new change` → 實作 → archive → 在這裡勾選 → commit。
