# OmniUI Roadmap

> 中文版：[ROADMAP.zh-TW.md](ROADMAP.zh-TW.md)

This file tracks planned features and improvements. Check off items as they are implemented and archived.

---

## ✅ Done

- Login flow automation (`click`, `type`, `verify_text`)
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
- TreeTableView (select row, read cell, expand/collapse)
- ColorPicker (`set_color`, `get_color`, `open_colorpicker`, `dismiss_colorpicker`)
- SplitPane (`get_divider_positions`, `set_divider_position`)

---

## 🔥 High Priority — Common Test Pain Points

- [ ] **Scroll** — `scroll_to(id=...)`, `scroll_by(amount, direction)`
  - Needed for ScrollPane, ListView, and long TableView content
- [ ] **Keyboard shortcuts** — `press_key("Ctrl+C")`, `press_key("Enter")`, `press_key("Escape")`
  - Fills the keyboard interaction gap; important for Dialog and TextField flows
- [ ] **Double-click** — `double_click(id=...)`
  - Commonly used to expand TreeView / TableView nodes
- [ ] **Close application** — `close_app()`
  - Calls `Platform.exit()` from within the agent; useful for test teardown and shutdown behavior testing
- [ ] **Wait conditions** — `wait_for_text(id, expected, timeout)`, `wait_for_visible(id, timeout)`, `wait_for_enabled(id, timeout)`, `wait_for_node(id, timeout)`, `wait_for_value(id, expected, timeout)`
  - Poll-based (Python-side) or agent-side blocking; essential for async UI state changes

---

## 🧩 Medium Priority — Additional Controls

- [ ] **Node state queries** — `is_visible(id=...)`, `is_enabled(id=...)`
  - Agent already includes `visible` / `enabled` in discovery metadata; Python-side convenience wrappers only
- [ ] **Tooltip verification** — `get_tooltip(id=...)`
  - Read the tooltip text of a node
- [ ] **Pagination** — `get_page()`, `set_page(n)`, `next_page()`, `prev_page()`
  - Automate JavaFX `Pagination` control
- [ ] **Window / Stage management** — `get_windows()`, `focus_window(title=...)`
  - Multi-window test scenarios

---

## 🏗️ Infrastructure / DX

- [ ] **Flexible `verify_text`** — support `contains=`, `starts_with=`, `regex=` match modes
  - Current exact-match only is often too strict
- [ ] **Video recording** — complement screenshot for richer debug output
- [ ] **Drag & Drop** — `drag(source_id, target_id)`
- [ ] **Hover** — `hover(id=...)` to trigger tooltips or hover states
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
