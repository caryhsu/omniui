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

- [ ] **捲動控制** — `scroll_to(id=...)`、`scroll_by(amount, direction)`
  - ScrollPane、ListView、長 TableView 都需要
- [ ] **鍵盤快捷鍵** — `press_key("Ctrl+C")`、`press_key("Enter")`、`press_key("Escape")`
  - 補完鍵盤操作缺口，對 Dialog / TextField 流程很重要
- [x] **雙擊** — `double_click(id=...)`
  - TreeView / TableView 展開節點的常用操作
- [x] **關閉應用程式** — `close_app()`
  - 從 agent 內呼叫 `Platform.exit()`；適合測試收尾與關閉行為驗證
- [x] **等待條件（waitFor）** — `wait_for_text(id, expected, timeout)`、`wait_for_visible(id, timeout)`、`wait_for_enabled(id, timeout)`、`wait_for_node(id, timeout)`、`wait_for_value(id, expected, timeout)`
  - Python 端輪詢或 agent 端阻塞；非同步 UI 狀態變更不可或缺

---

## 🧩 中等優先 — 補充控制項

- [x] **節點狀態查詢** — `is_visible(id=...)`、`is_enabled(id=...)`
  - Agent 的節點探索已包含 `visible` / `enabled`；只需在 Python 端加上便利封裝
- [ ] **Tooltip 驗證** — `get_tooltip(id=...)`
  - 讀取節點的 Tooltip 文字
- [ ] **多選（Multi-select）** — `select_multiple(id=..., values=[...])`
  - ListView / TableView 多項目選取；目前 `select` 僅支援單選
- [ ] **TableView 儲存格編輯** — `edit_cell(id=..., row=..., column=..., value=...)`
  - 雙擊儲存格並輸入新值（需要 editable TableView）
- [ ] **TableView 欄位排序** — `sort_column(id=..., column=..., direction="asc")`
  - 點擊欄位標題觸發排序並讀回結果
- [ ] **ToolBar** — `get_toolbar_items(id=...)`，透過現有 `click` 操作工具列按鈕
  - 存取與操作 `ToolBar` 容器中的項目
- [ ] **ScrollBar** — `get_scroll_position(id=...)`、`set_scroll_position(id=..., value=...)`
  - 對獨立 `ScrollBar` 節點進行精細捲動控制
- [ ] **Pagination（分頁）** — `get_page()`、`set_page(n)`、`next_page()`、`prev_page()`
  - 自動化 JavaFX `Pagination` 控件
- [ ] **Window / Stage 管理** — `get_windows()`、`focus_window(title=...)`
  - 多視窗測試場景

---

## 🏗️ 基礎建設 / 開發體驗

- [ ] **`verify_text` 彈性比對** — 支援 `contains=`、`starts_with=`、`regex=` 模式
  - 目前只有 exact match，常常過於嚴格
- [ ] **錄影功能** — 補充 screenshot 以外的偵錯工具
- [ ] **拖放（Drag & Drop）** — `drag(source_id, target_id)`
- [ ] **Hover（懸停）** — `hover(id=...)` 觸發 Tooltip 或 hover 狀態
- [ ] **剪貼簿操作** — `copy()`、`paste()`、`get_clipboard()`

---

## 💡 構想 / 未來規劃

- [ ] **FileChooser / DirectoryChooser** 自動化
- [ ] **焦點管理** — `tab_focus()`、`verify_focused(id=...)`
- [ ] **無障礙檢查** — 驗證 ARIA 角色與標籤
- [ ] **WebView** 自動化（執行 JavaScript）
- [ ] **效能指標** — 操作計時、畫面更新頻率

---

> **工作流程：** 挑一個項目 → `openspec new change` → 實作 → archive → 在這裡勾選 → commit。
