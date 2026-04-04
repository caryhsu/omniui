# OmniUI Roadmap

This file tracks planned features and improvements. Check off items as they are implemented and archived.

---

## ✅ Done

- Login flow automation (click, type, verify_text)
- ComboBox, ListView, TreeView, TableView selection
- ContextMenu, MenuBar
- DatePicker (`open_datepicker`, `set_date`, `pick_date`, `navigate_month`)
- Dialog / Alert (`get_dialog`, `dismiss_dialog`)
- RadioButton / ToggleButton (`get_selected`, `set_selected`)
- Slider / Spinner (`set_slider`, `set_spinner`, `step_spinner`)
- ProgressBar / ProgressIndicator (`get_progress`, `get_value`)
- TabPane (`get_tabs`, `select_tab`)
- TextArea, PasswordField
- Hyperlink (`is_visited`)
- CheckBox, ChoiceBox
- Accordion / TitledPane (`expand_pane`, `collapse_pane`, `get_expanded`)
- TreeTableView (select, read cell, expand/collapse)
- ColorPicker (`set_color`, `get_color`, `open_colorpicker`, `dismiss_colorpicker`)
- SplitPane (`get_divider_positions`, `set_divider_position`)

---

## 🔥 High Priority — Common Test Pain Points

- [ ] **Scroll / 捲動控制** — `scroll_to(id=...)`, `scroll_by(amount, direction)`
  - ScrollPane, ListView, TableView 長列表都需要
- [ ] **Keyboard shortcuts** — `press_key("Ctrl+C")`, `press_key("Enter")`, `press_key("Escape")`
  - 補完鍵盤操作缺口，對 Dialog / TextField 很重要
- [ ] **Double-click** — `double_click(id=...)`
  - TreeView / TableView 展開節點常用

---

## 🧩 Medium Priority — Additional Controls

- [ ] **Tooltip 驗證** — `get_tooltip(id=...)`
  - 讀取節點的 Tooltip 文字
- [ ] **Pagination** — `get_page()`, `set_page(n)`, `next_page()`, `prev_page()`
  - JavaFX `Pagination` 控件
- [ ] **Window / Stage 管理** — `get_windows()`, `focus_window(title=...)`
  - 多視窗測試場景

---

## 🏗️ Infrastructure / DX

- [ ] **`verify_text` 彈性比對** — 支援 `contains=`, `starts_with=`, `regex=` 模式
  - 目前只有 exact match，常常過於嚴格
- [ ] **Video recording** — 補充 screenshot 以外的偵錯工具
- [ ] **Drag & Drop** — `drag(source_id, target_id)`
- [ ] **Hover** — `hover(id=...)` + Tooltip 觸發
- [ ] **Clipboard operations** — `copy()`, `paste()`, `get_clipboard()`

---

## 💡 Ideas / Future

- [ ] **FileChooser / DirectoryChooser** automation
- [ ] **Focus management** — `tab_focus()`, `verify_focused(id=...)`
- [ ] **Accessibility checks** — verify ARIA roles / labels
- [ ] **WebView** automation (JavaScript execution)
- [ ] **Performance metrics** — action timing, frame rate

---

> **Workflow:** Pick an item → `openspec new change` → implement → archive → check it off here → commit.
