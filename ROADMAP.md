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

- [x] **Scroll** — `scroll_to(id=...)`, `scroll_by(delta_x, delta_y, id=...)`
  - Supports ScrollPane; `scroll_to` walks parent chain; `scroll_by` uses normalised 0–1 offset
- [x] **Keyboard shortcuts** — `press_key("Ctrl+C")`, `press_key("Enter")`, `press_key("Escape")`
  - Fills the keyboard interaction gap; important for Dialog and TextField flows
- [x] **Double-click** — `double_click(id=...)`
  - Commonly used to expand TreeView / TableView nodes
- [x] **Close application** — `close_app()`
  - Calls `Platform.exit()` from within the agent; useful for test teardown and shutdown behavior testing
- [x] **Wait conditions** — `wait_for_text(id, expected, timeout)`, `wait_for_visible(id, timeout)`, `wait_for_enabled(id, timeout)`, `wait_for_node(id, timeout)`, `wait_for_value(id, expected, timeout)`
  - Poll-based (Python-side) or agent-side blocking; essential for async UI state changes

---

## 🧩 Medium Priority — Additional Controls

- [x] **Node state queries** — `is_visible(id=...)`, `is_enabled(id=...)`
  - Agent already includes `visible` / `enabled` in discovery metadata; Python-side convenience wrappers only
- [x] **Tooltip verification** — `get_tooltip(id=...)`
  - Read the tooltip text of a node
- [x] **CSS style inspection** — `get_style(id=...)`, `get_style_class(id=...)`
  - `get_style()` returns the node's inline style string (e.g. `"-fx-text-fill: red;"`)
  - `get_style_class()` returns the list of CSS class names applied to the node
  - Enables validation state testing: red/green color, error/success CSS class, without depending on a specific label text
- [x] **Multi-select** — `select_multiple(id=..., values=[...])`
  - Select multiple items in ListView or TableView; `get_selected_items(id=...)` returns all selected items as a list
- [ ] **Modifier+click** — `click(id=..., modifiers=["Ctrl"])`, `click(id=..., modifiers=["Ctrl", "Shift"])`
  - Ctrl+click for additive selection, Shift+click for range selection; useful for ListView/TableView multi-select workflows without needing a separate `select_multiple` call
- [ ] **TableView in-cell editing** — `edit_cell(id=..., row=..., column=..., value=...)`
  - Double-click a cell and type a new value (requires editable TableView)
- [ ] **TableView column sort** — `sort_column(id=..., column=..., direction="asc")`
  - Click a column header to trigger sort; read sorted order back
- [x] **index= selector** — `click(type="Button", index=0)`, `click(id="myList", index=2)`
  - Select the Nth matching node when there is no unique id; solves dynamic/generated nodes (ListView cells, repeated controls)
- [ ] **TableView positional cell access** — `get_cell(id=..., row=N, column=N)`, `click_cell(id=..., row=N, column=N)`
  - Access a specific cell by row/column index, not just by value; needed for dynamic table content without unique ids
- [ ] **ToolBar** — `get_toolbar_items(id=...)`, click toolbar buttons via existing `click`
  - Access and interact with items in a `ToolBar` container
- [ ] **ScrollBar** — `get_scroll_position(id=...)`, `set_scroll_position(id=..., value=...)`
  - Fine-grained scroll control for standalone `ScrollBar` nodes
- [ ] **Pagination** — `get_page()`, `set_page(n)`, `next_page()`, `prev_page()`
  - Automate JavaFX `Pagination` control
- [ ] **Window / Stage management** — `get_windows()`, `focus_window(title=...)`
  - Multi-window test scenarios

---

## 🏗️ Infrastructure / DX

- [x] **Flexible `verify_text`** — support `match="contains"`, `match="starts_with"`, `match="regex"` modes
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
