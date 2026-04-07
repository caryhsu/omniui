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
- [x] **App launch API** — `launch_app(jar=..., port=...)` start a JavaFX app (with embedded agent) directly from Python
  - Currently the agent must be started manually before any test; this bridges the gap vs Playwright / WinAppDriver

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
- [x] **Modifier+click** — `click(id=..., modifiers=["Ctrl"])`, `click(id=..., modifiers=["Ctrl", "Shift"])`
  - Ctrl+click for additive selection, Shift+click for range selection; useful for ListView/TableView multi-select workflows without needing a separate `select_multiple` call
- [x] **TableView in-cell editing** — `edit_cell(id=..., row=..., column=..., value=...)`
  - Double-click a cell and type a new value (requires editable TableView)
- [x] **TableView column sort** — `sort_column(id=..., column=..., direction="asc")`
  - Click a column header to trigger sort; read sorted order back
- [x] **index= selector** — `click(type="Button", index=0)`, `click(id="myList", index=2)`
  - Select the Nth matching node when there is no unique id; solves dynamic/generated nodes (ListView cells, repeated controls)
- [x] **TableView positional cell access** — `get_cell(id=..., row=N, column=N)`, `click_cell(id=..., row=N, column=N)`
  - Access a specific cell by row/column index, not just by value; needed for dynamic table content without unique ids
- [x] **ToolBar** — `get_toolbar_items(id=...)`, click toolbar buttons via existing `click`
  - Access and interact with items in a `ToolBar` container
- [x] **ScrollBar** — `get_scroll_position(id=...)`, `set_scroll_position(id=..., value=...)`
  - Fine-grained scroll control for standalone `ScrollBar` nodes
- [x] **Pagination** — `get_page()`, `set_page(n)`, `next_page()`, `prev_page()`
  - Automate JavaFX `Pagination` control
- [x] **Window / Stage management** — `get_windows()`, `focus_window(title=...)`
  - Multi-window test scenarios
  - `maximize_window()`, `minimize_window()`, `restore_window()`
  - `set_window_size(width, height)`, `set_window_position(x, y)`
  - `is_maximized()`, `is_minimized()`, `get_window_size()`, `get_window_position()`
  - Implemented via JavaFX `Stage` reflective calls (`setMaximized`, `setIconified`, `setX/Y`, etc.)
- [x] **Absolute coordinate click** — `click_at(x=100, y=200)`
  - Fallback for Canvas-rendered or custom-drawn UI elements that have no scene graph node
- [x] **Scoped selector (`within`)** — `with client.within(id="panel"): client.click(id="btn")`
  - Restrict node search to a subtree; avoids conflicts when multiple panels share the same child IDs
- [x] **Scene graph snapshot** — `client.snapshot()` captures full UI node state as a structured list
- [x] **Scene graph diff** — `client.diff(before, after)` compares two snapshots and returns added/removed/changed nodes
  - More thorough than `verify_text`; useful for verifying the side-effects of a single action

---

## 🏗️ Infrastructure / DX

- [x] **Flexible `verify_text`** — support `match="contains"`, `match="starts_with"`, `match="regex"` modes
- [x] **Locator object** — `loc = client.locator(id="btn")` returns a reusable handle; call `loc.click()`, `loc.verify_text(...)`, `loc.wait_for_visible()` without repeating the selector
  - Enables Page Object Model and reduces selector duplication
- [x] **Page Object Model base class** — `OmniPage` base class with auto-wired `client`; gives test projects a standard structure
- [x] **Soft assertions** — `with client.soft_assert() as sa:` collects all failures and reports them together instead of stopping at the first
- [x] **Retry helper** — `@client.retry(times=3, delay=0.5)` decorator for flaky assertion blocks
- [x] **Structured action trace** — every action logged with timestamp, selector, and result; printed as a timeline on test failure
- [x] **Parallel test support** — document and example for running multiple `OmniUI` clients against separate app instances with `pytest-xdist`
  - Worker-aware `omniui_parallel` fixture; `_worker_port()` helper; `tests/conftest_parallel_example.py`; `tests/test_parallel_example.py`; `docs/parallel-testing.md`
- [x] **pytest fixture integration** — `@pytest.fixture` that auto-connects and disconnects; keeps test boilerplate minimal
- [x] **Auto-screenshot on failure** — automatically capture and save a screenshot when any action raises an exception
- [x] **Custom wait condition** — `wait_until(fn, timeout)` accepts a user-supplied lambda for arbitrary poll logic
- [x] **Headless mode** — Xvfb virtual display on Linux; `run_all.py` auto-detects OS and selects correct JavaFX JARs; see `docs/headless.md`
- [x] **CI/CD examples** — GitHub Actions workflows: `ci-unit.yml` (pytest only) and `ci-integration.yml` (Xvfb + full demo suite)
- [x] **HTML test report** — pytest-html integration; screenshots embedded inline on failure; see `docs/html-report.md`
- [ ] **Video recording** — complement screenshot for richer debug output
- [ ] **Self-healing selector** — when a locator fails because `fx:id` was renamed or removed, automatically retry using `text` then `type+index` fallbacks before raising an error; log which fallback was used so developers know to update the selector.
- [x] **Drag & Drop** — `drag(id=...).to(id=...)` / `drag_to(id=..., to_x=..., to_y=...)`; fires MOUSE_PRESSED → MOUSE_DRAGGED × 5 → MOUSE_RELEASED
- [ ] **Dedicated drag-drop demo app** — standalone JavaFX app with two ListViews (left → right item transfer) as a richer, real-world drag & drop showcase; replaces the cramped section inside advanced-app
- [x] **Dedicated drag-drop demo app** — `demo/java/drag-app/` standalone app; left panel (Available) → right panel (Selected) with per-item drag; `demo/python/drag/drag_listview_demo.py`
- [x] **Hover** — `hover(id=...)` to trigger tooltips or hover states
- [x] **Clipboard operations** — `copy()`, `paste()`, `get_clipboard()`

---

## 🎬 Full Recorder

- [x] **Event capture** — Java agent attaches `EventFilter` to the Scene to intercept mouse clicks, double-clicks, key presses, and text input
- [x] **Selector inference** — derive the best selector from the clicked node: `fx:id` → `text` → `type + index`
- [x] **Script generation** — Python-side generator serialises recorded events into a runnable test script
  - Outputs `click`, `type`, `press_key`, `verify_text`, etc.
  - Sensitive fields (e.g. `PasswordField`) are masked with a placeholder
- [x] **Wait injection** — heuristically insert `wait_for_*` calls between actions; `start_recording(wait_injection=True)`
  - Button/ComboBox/CheckBox/... → `wait_for_enabled`; other nodes → `wait_for_visible`
  - Fragile selectors (no `fx:id` / `text`) are skipped
- [x] **Record session API** — `start_recording(wait_injection)` / `stop_recording()` / `save_script(path)`
- [ ] **Recorder UI tool** — ~~interactive TUI/GUI app (`python -m omniui.recorder`) with Record / Stop / Save controls, allowing non-programmers to record sessions without writing code~~
- [x] **Recorder UI tool** — `python -m omniui.recorder` tkinter GUI; auto-scans running apps, Record / Stop / Save workflow
  - [x] **Wait injection checkbox** — "Insert wait_for_*" toggle in GUI; setting applied at Record time
- [ ] **Drag & Drop recording** — capture `MOUSE_PRESSED` + `MOUSE_RELEASED` pairs in the Recorder; infer source/target nodes and emit `client.drag(id=...).to(id=...)` in generated scripts
  - [x] **Drag & Drop recording** — `MOUSE_PRESSED/RELEASED` filters; `PickResult` for drop target; `dragJustFired` suppresses spurious click; generates `client.drag(...).to(...)`
- [x] **Drag & Drop recording** — `MOUSE_PRESSED`+`MOUSE_RELEASED` pair with ≥15 px distance emits `drag` event; codegen outputs `client.drag(id=...).to(id=...)`
- [x] **Real-time recording feedback** — currently the Recorder GUI only shows the captured script after pressing Stop. Add a polling mechanism (`GET /sessions/{sessionId}/events/pending` every ~500 ms) so each user action appears in the script preview immediately as it is recorded. Upgrade path: polling first, then SSE if sub-100 ms latency or a browser-based UI is needed later.
- [x] **Record & Run** — Run All and Run Selection buttons in Recorder GUI; executes recorded script against the connected agent via `exec()`
- [ ] **Assertion recording** — while recording, right-click any element to insert a `verify_text` / `verify_visible` / `verify_enabled` assertion step; recorded assertions appear inline in the generated script alongside interaction steps.
- [ ] **Step editor in Recorder** — after stopping, allow deleting or reordering individual steps in the script preview before saving; drag-to-reorder rows or select + delete unwanted steps.
- [ ] **Screenshot on failure** — when a script step raises an exception, automatically capture a screenshot and attach it to the error output; pairs with Record & Run and CI reporting.
- [ ] **Locator Inspector** — click any element in the running app to display all available selectors (`fx:id`, `text`, `type+index`) ranked by stability; lets developers verify or copy the best selector without writing code.

### Known Recorder Issues

- [x] **fragile selector in dialogs** — Clicks on internal layout nodes (e.g. spacing `Pane` inside a dialog) are recorded as `click(type="Pane", index=0)` because they have no `fx:id` or stable text. These are marked with `# WARN: fragile selector` but still produce unreliable scripts. Fix: suppress recording non-actionable layout nodes that are not Button/Label/TextField/etc. Also fixed `dismiss_dialog` not being recorded for Dialog OK/Cancel buttons by directly attaching ACTION event handlers to ButtonBar buttons. *(merged PR #19)*

---

## 🧪 Testing Infrastructure

- [ ] **e2e demo scripts not in pytest** — `demo/python/*/` demo scripts are real end-to-end tests that launch apps and exercise the full stack, but they are not collected by `pytest tests/`. Only `test_parallel_example.py` uses `OmniUI.launch()` against a real app. All other `tests/` are Python unit tests (mocks). Fix: wrap each demo script as a pytest integration test with `@pytest.mark.integration`, and add `--integration` flag to CI so the full e2e suite can be opted into separately.

---

## 💡 Ideas / Future

- [x] **Split login-app from core-app** — `core-app` currently bundles the login demo with other core controls (ListView, TableView, ComboBox, etc.). Extract the login form into a dedicated `demo/java/login-app` so that `core-app` stays focused on control coverage and `login-app` becomes a standalone example of authentication flow automation (`click`, `type`, `verify_text`). ✅
- [ ] **Mini Explorer demo app** — a fake file-browser app (`demo/java/explorer-app`) to exercise TreeView navigation and horizontal SplitPane divider control. Layout: left panel = TreeView with mock folder/file hierarchy; right panel = file list updating on selection; user-adjustable divider. Covers: `select_tree_item`, `get_tree_items`, `get_divider_positions`, `set_divider_position`.
- [ ] **Settings Panel demo app** — a settings form app (`demo/java/settings-app`) to exercise multiple input controls in a realistic layout. Suggested controls: TextField, PasswordField, ComboBox, CheckBox, RadioButton, Slider, Spinner, ToggleButton, plus Save / Reset buttons. Can be single-page or organized with TabPane (e.g. Account / Appearance / Notifications tabs).
- [ ] **Multi-app automation** — orchestrate multiple JavaFX apps in a single test flow
  - Multiple agent connections managed from Python (each JavaFX app embeds its own agent)
  - App lifecycle: launch, connect, switch, disconnect, close
  - App identification by port, PID, or named alias
  - API design TBD: `client.use("app1").click(...)` and/or multiple `OmniUI` instances
  - Cross-app workflows: e.g. fill form in app A, verify result in app B
- [ ] **VS Code Extension (Recorder)** — a VS Code extension providing a sidebar panel with Record / Stop / Run controls. Communicates with the Java agent via the existing HTTP API (no agent changes needed). Key capabilities over the Tkinter GUI: insert recorded code at the current cursor position in the active editor; Run Selection maps to VS Code's native terminal execution; syntax highlighting and IntelliSense for the generated script immediately available. The Tkinter GUI and VS Code extension can coexist — the extension targets developers already in VS Code.
- [x] **Focus management** — `tab_focus()`, `verify_focused(id=...)`
- [ ] **Accessibility checks** — verify ARIA roles / labels
- [ ] **WebView** automation (JavaScript execution)
- [ ] **Performance metrics** — action timing, frame rate

---

> **Workflow:** Pick an item → `openspec new change` → implement → archive → check it off here → commit.
