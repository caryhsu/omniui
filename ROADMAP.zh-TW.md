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
- [ ] **Pagination（分頁）** — `get_page()`、`set_page(n)`、`next_page()`、`prev_page()`
  - 自動化 JavaFX `Pagination` 控件
- [ ] **Window / Stage 管理** — `get_windows()`、`focus_window(title=...)`
  - 多視窗測試場景
  - `maximize_window()`、`minimize_window()`、`restore_window()`
  - `set_window_size(width, height)`、`set_window_position(x, y)`
  - `is_maximized()`、`is_minimized()`、`get_window_size()`、`get_window_position()`
  - 透過 JavaFX `Stage` reflective call 實作（`setMaximized`、`setIconified`、`setX/Y` 等）
- [x] **絕對座標點擊** — `click_at(x=100, y=200)`
  - Canvas 自繪或無 scene graph 節點的 UI 元素的 fallback 操作方式
- [ ] **範圍限定選擇器（`within`）** — `with client.within(id="panel"): client.click(id="btn")`
  - 將節點搜尋限定在子樹範圍；避免多個 panel 共用相同子節點 ID 時發生衝突
- [ ] **Scene graph 快照 / 差異比對** — `client.snapshot()` 擷取完整 UI 狀態；`client.diff(before, after)` 顯示哪些節點發生變化
  - 比 `verify_text` 更全面；適合驗證單一操作的所有副作用

---

## 🏗️ 基礎建設 / 開發體驗

- [x] **`verify_text` 彈性比對** — 支援 `match="contains"`、`match="starts_with"`、`match="regex"` 模式
  - 目前只有 exact match，常常過於嚴格
- [x] **Locator 物件** — `loc = client.locator(id="btn")` 回傳可重複使用的元素 handle；可呼叫 `loc.click()`、`loc.verify_text(...)`、`loc.wait_for_visible()` 而不需重複寫 selector
  - 支援 Page Object Model，減少 selector 重複
- [ ] **Page Object Model 基礎類別** — `OmniPage` base class 自動注入 `client`；給測試專案標準化結構
- [x] **Soft Assertions** — `with client.soft_assert() as sa:` 收集所有失敗後一次回報，不會第一個就中斷
- [x] **Retry 輔助器** — `@client.retry(times=3, delay=0.5)` 裝飾器，用於不穩定的 assertion 區塊
- [x] **結構化動作 Trace** — 每個操作自動記錄 timestamp、selector、結果；測試失敗時輸出完整動作時間軸
- [ ] **平行測試支援** — 說明文件與範例，搭配 `pytest-xdist` 對多個 app 實例執行平行測試
- [x] **pytest fixture 整合** — `@pytest.fixture` 自動 connect/disconnect，減少測試樣板程式碼
- [x] **失敗自動截圖** — 任何操作拋出例外時自動擷取並儲存截圖
- [x] **自訂 wait 條件** — `wait_until(fn, timeout)` 接受自訂 lambda，支援任意輪詢邏輯
- [ ] **Headless 模式** — 說明並支援 JavaFX Monocle headless 模式，讓 CI 不需要顯示器
- [ ] **CI/CD 範例** — GitHub Actions workflow 範本（headless + agent 啟動 + pytest）
- [ ] **HTML 測試報告** — pytest-html 或 Allure 整合說明
- [ ] **錄影功能** — 補充 screenshot 以外的偵錯工具
- [ ] **拖放（Drag & Drop）** — `drag(source_id, target_id)`
- [x] **Hover（懸停）** — `hover(id=...)` 觸發 Tooltip 或 hover 狀態
- [x] **剪貼簿操作** — `copy()`、`paste()`、`get_clipboard()`

---

## 🎬 完整錄製器（Full Recorder）

- [ ] **事件捕捉** — Java agent 在 Scene 上掛 `EventFilter`，攔截滑鼠點擊、雙擊、按鍵與文字輸入
- [ ] **Selector 推導** — 從被點擊的節點自動選出最佳 selector：`fx:id` → `text` → `type + index`
- [ ] **腳本生成** — Python 端將錄製事件序列化為可執行的測試腳本
  - 輸出 `click`、`type`、`press_key`、`verify_text` 等指令
  - 敏感欄位（如 `PasswordField`）自動遮罩，替換為佔位符
- [ ] **Wait 自動插入** — 啟發式判斷哪些操作後需要等待非同步 UI 變更，自動插入 `wait_for_*`
- [ ] **錄製 Session API** — `start_recording()` / `stop_recording()` / `save_script(path)`

---

## 💡 構想 / 未來規劃

- [ ] **多 App 自動化（Multi-app automation）** — 在單一測試流程中協調多個 JavaFX app
  - Python 端管理多條 agent 連線（每個 JavaFX app 各自嵌入一個 agent）
  - App 生命週期管理：啟動、連線、切換、斷線、關閉
  - App 識別方式：port、PID 或具名 alias
  - API 設計待定：`client.use("app1").click(...)` 或多個 `OmniUI` 實例，或兩者皆支援
  - 跨 App 工作流：例如在 app A 填寫表單，在 app B 驗證結果
- [ ] **視覺回歸測試** — 截圖 baseline 比對，偵測非預期的 UI 變更
- [x] **焦點管理** — `tab_focus()`、`verify_focused(id=...)`
- [ ] **無障礙檢查** — 驗證 ARIA 角色與標籤
- [ ] **WebView** 自動化（執行 JavaScript）
- [ ] **效能指標** — 操作計時、畫面更新頻率

---

> **工作流程：** 挑一個項目 → `openspec new change` → 實作 → archive → 在這裡勾選 → commit。
