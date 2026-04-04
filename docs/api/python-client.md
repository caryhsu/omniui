# OmniUI Python Client API

This document describes the currently implemented Python API surface for OmniUI Phase 1.

## Overview

Main entry point:

```python
from omniui import OmniUI
```

Session factory:

```python
client = OmniUI.connect(...)
```

Concrete session type:
- `OmniUIClient`

Core model types:
- `Selector`
- `ActionResult`
- `ActionTrace`
- `ResolvedElement`
- `ActionLogEntry`

## Connection

### `OmniUI.connect(base_url="http://127.0.0.1:48100", app_name="LoginDemo", pid=None, ocr_engine=None, vision_engine=None)`

Create a client session against the local OmniUI agent.

Parameters:
- `base_url: str`
  Local OmniUI agent base URL.
- `app_name: str`
  Logical target application name sent to the Java agent session endpoint.
- `pid: int | None`
  Optional process identifier for future attach scenarios.
- `ocr_engine: SimpleOcrEngine | None`
  Optional OCR provider implementation.
- `vision_engine: SimpleVisionEngine | None`
  Optional vision provider implementation.

Returns:
- `OmniUIClient`

Behavior:
- Calls `GET /health`
- Calls `POST /sessions`
- Raises `RuntimeError` if the agent does not report healthy status

## Session API

### `client.get_nodes() -> list[dict[str, Any]]`

Fetch a JavaFX node snapshot from the agent.

Current node fields include:
- `handle`
- `fxId`
- `nodeType`
- `text`
- `hierarchyPath`
- `visible`
- `enabled`

### `client.find(id=None, text=None, type=None, index=None) -> dict[str, Any]`

Normalize selector input without executing any action.

Current selector fields:
- `id`
- `text`
- `type`
- `index` — 0-based integer; picks the Nth node among all nodes matching the other fields (default: 0)

Normalization rules:
- empty strings are converted to `None`
- values are stripped before use

### `client.click(**selector) -> ActionResult`

Execute a click action.

Resolution order:
1. JavaFX direct resolution through the agent
2. refresh + retry if the initial failure reason is `selector_not_found`
3. OCR fallback for text selectors
4. vision fallback if OCR does not resolve and a `template` is provided

Notes:
- In the current repo, OCR and vision fallback resolve the target and trace it, but do not yet dispatch a real OS-level click.
- For JavaFX-resolved nodes, the Java agent uses direct node-level interaction.

### `client.type(text, **selector) -> ActionResult`

Execute a text input action through the JavaFX path.

Parameters:
- `text: str`
- selector fields such as `id`, `text`, `type`

### `client.get_text(**selector) -> ActionResult`

Resolve an element and fetch its text through the JavaFX path.

### `client.get_tooltip(**selector) -> ActionResult`

Read the tooltip text of a node.

- `result.value` — the tooltip text string, or `""` if the node has no tooltip attached
- `result.ok` is `False` (with reason `selector_not_found`) if the node cannot be resolved

```python
tip = client.get_tooltip(id="submitButton")
assert tip.value == "Click to submit the form"

# Node without a tooltip
tip2 = client.get_tooltip(id="statusLabel")
assert tip2.value == ""
```

### `client.get_style(**selector) -> ActionResult`

Read the **inline** CSS style string of a node (`node.getStyle()`).

- `result.value` — inline style string (e.g. `"-fx-text-fill: red;"`) or `""` if not set
- Only reflects styles set via `setStyle()` — stylesheet-applied styles are **not** returned

```python
style = client.get_style(id="validationLabel")
assert "-fx-text-fill: red" in style.value

# No inline style
style2 = client.get_style(id="status")
assert style2.value == ""
```

### `client.get_style_class(**selector) -> ActionResult`

Read the list of CSS class names applied to a node (`node.getStyleClass()`).

- `result.value` — `list[str]` of CSS class names (e.g. `["button", "error"]`)

```python
classes = client.get_style_class(id="loginButton")
assert "button" in classes.value

# Validation error class
classes2 = client.get_style_class(id="idField")
assert "error" in classes2.value
```

### `client.verify_text(expected, *, match="exact", **selector) -> ActionResult`

Resolve an element, fetch its text, and compare it to `expected` using the given `match` mode.

| `match` value | Behaviour |
|---|---|
| `"exact"` *(default)* | `actual == expected` |
| `"contains"` | `expected in actual` |
| `"starts_with"` | `actual.startswith(expected)` |
| `"regex"` | `re.search(expected, actual)` — matches anywhere in the string |

Raises `ValueError` for unknown `match` values.

Return semantics:
- `result.ok` is `True` only when the comparison succeeds
- `result.value`:

```python
{
    "actual": ...,
    "expected": ...,
    "match": "exact" | "contains" | "starts_with" | "regex",
    "matches": True | False,
}
```

**Examples**

```python
client.verify_text("Login Flow", id="loginSectionTitle")                   # exact (default)
client.verify_text("Login", match="contains", id="loginSectionTitle")      # substring
client.verify_text("Login", match="starts_with", id="loginSectionTitle")   # prefix
client.verify_text(r"^Login \w+$", match="regex", id="loginSectionTitle")  # regex
```

---

## Scroll Actions

### `client.scroll_to(**selector) -> ActionResult`

Find the nearest `ScrollPane` ancestor of the resolved node and adjust its `vvalue` so the node is visible in the viewport.

The `vvalue` is computed from the node's position within the scroll content:

```
vvalue = nodeTop / (contentHeight - viewportHeight)   # clamped 0–1
```

**Example**

```python
client.scroll_to(id="scrollRow29")   # bring row 29 into view
```

---

### `client.scroll_by(delta_x, delta_y, **selector) -> ActionResult`

Adjust the `hvalue` / `vvalue` of the resolved `ScrollPane` by a relative offset. Both `delta_x` and `delta_y` are in the 0.0–1.0 normalised range.

- Positive `delta_y` → scroll down; negative → scroll up
- If no selector is given the first `ScrollPane` found in the scene is used

**Examples**

```python
client.scroll_by(0.0,  0.2, id="myScrollPane")   # scroll down 20%
client.scroll_by(0.0, -1.0, id="myScrollPane")   # scroll back to top
client.scroll_by(0.0,  0.5)                       # first ScrollPane in scene
```

---

## Menu and ContextMenu Actions

### `client.right_click(**selector) -> ActionResult`

Right-click a node to open its context menu. Waits for the context menu overlay to appear.

### `client.double_click(**selector) -> ActionResult`

Fire a double-click (synthesized `MouseEvent.MOUSE_CLICKED` with `clickCount=2`) on the target node.

Use for interactions such as expanding TreeView items, opening detail views from ListView/TableView rows, or any custom `setOnMouseClicked` handler that inspects `event.getClickCount() == 2`.

```python
client.double_click(id="myTreeItem")
client.double_click(text="Record 1", type="ListCell")
```

### `client.press_key(key, **selector) -> ActionResult`

Fire `KEY_PRESSED` + `KEY_RELEASED` for the given key string.

**Format:** `"[Modifier+]*Key"` — case-insensitive. Aliases: `Ctrl` = `Control`, `Win` = `Meta`.

| Example | Meaning |
|---------|---------|
| `"Escape"` | Press Escape key |
| `"Enter"` | Press Enter key |
| `"Tab"` | Press Tab key |
| `"Control+C"` | Ctrl+C |
| `"ctrl+z"` | Ctrl+Z (alias, lowercase OK) |
| `"Shift+Tab"` | Shift+Tab |
| `"Control+Shift+Z"` | Ctrl+Shift+Z |

If **selector** is provided, the event fires on that node. If omitted, it fires on the scene's current focus owner.

```python
client.press_key("Escape")                    # global, no selector
client.press_key("Tab", id="username")        # on specific node
client.press_key("Control+A", id="username")  # select all
client.press_key("ctrl+z")                    # alias accepted
```

### `client.open_menu(menu, **selector) -> ActionResult`

Open a top-level menu in a `MenuBar`. Waits for the menu popup to appear.

Parameters:
- `menu: str` — label of the top-level menu item (e.g. `"File"`)
- selector fields to identify the MenuBar node

### `client.navigate_menu(path, **selector) -> ActionResult`

Open a `MenuBar` menu and navigate to a nested item.

Parameters:
- `path: str` — slash-separated path (e.g. `"Edit/Advanced/Reformat"`)
- selector fields to identify the MenuBar node

### `client.click_menu_item(item) -> ActionResult`

Click a visible menu item by label. Requires a menu or context menu to already be open.

Parameters:
- `item: str` — exact label text of the menu item

### `client.dismiss_menu() -> ActionResult`

Close the currently open menu or context menu popup.

---

## DatePicker Actions

### `client.set_date(date, **selector) -> ActionResult`

Set a `DatePicker` value directly without opening the popup. Preferred over `open_datepicker` + `pick_date`.

Parameters:
- `date: str` — ISO-8601 date string, e.g. `"2025-09-15"`
- selector fields to identify the DatePicker node

### `client.open_datepicker(**selector) -> ActionResult`

Click a `DatePicker` to open its calendar popup. Waits for the popup to appear.

### `client.navigate_month(direction) -> ActionResult`

Navigate the open DatePicker calendar by one month. Requires the calendar popup to be open.

Parameters:
- `direction: str` — `"forward"` or `"backward"`

### `client.pick_date(date) -> ActionResult`

Click a date cell in the open DatePicker calendar.

Parameters:
- `date: str` — ISO-8601 date string

---

## Dialog / Alert Actions

### `client.get_dialog() -> ActionResult`

Read the currently visible `Alert` dialog. Returns button labels and content text.

`result.value` shape:

```python
{
    "title": "...",
    "header": "...",
    "content": "...",
    "buttons": ["OK", "Cancel", ...]
}
```

### `client.dismiss_dialog(button=None) -> ActionResult`

Close the currently visible Alert dialog.

Parameters:
- `button: str | None` — button label to click (e.g. `"OK"`). If `None`, clicks the default button.

---

## Selection Controls

### `client.select(value, **selector) -> ActionResult`

Select an item by value in a `ComboBox`, `ChoiceBox`, or `ListView`.

Parameters:
- `value: str` — item label to select

### `client.get_selected(**selector) -> ActionResult`

Read the selected state of a `CheckBox`, `RadioButton`, or `ToggleButton`.

`result.value`: `True` or `False`.

### `client.set_selected(value, **selector) -> ActionResult`

Set the selected state of a `CheckBox`, `RadioButton`, or `ToggleButton`.

Parameters:
- `value: bool`

---

## Slider, Spinner, Progress

### `client.set_slider(value, **selector) -> ActionResult`

Set the value of a `Slider`.

Parameters:
- `value: float`

### `client.set_spinner(value, **selector) -> ActionResult`

Set the value of a `Spinner` directly.

Parameters:
- `value: int | float`

### `client.step_spinner(steps, **selector) -> ActionResult`

Increment or decrement a `Spinner` by a number of steps.

Parameters:
- `steps: int` — positive to increment, negative to decrement

### `client.get_progress(**selector) -> ActionResult`

Read the current progress of a `ProgressBar` or `ProgressIndicator`.

`result.value`: `float` in range `[0.0, 1.0]`.

### `client.get_value(**selector) -> ActionResult`

Read the current value of a generic value-bearing node (e.g. `Slider`, `DatePicker`).

---

## Tab Actions

### `client.get_tabs(**selector) -> ActionResult`

List the tab labels of a `TabPane`.

`result.value`: `list[str]`.

### `client.select_tab(tab, **selector) -> ActionResult`

Select a tab by label in a `TabPane`.

Parameters:
- `tab: str` — exact tab label

---

## Accordion Actions

### `client.expand_pane(**selector) -> ActionResult`

Expand a `TitledPane` inside an `Accordion`.

### `client.collapse_pane(**selector) -> ActionResult`

Collapse a `TitledPane` inside an `Accordion`.

### `client.get_expanded(**selector) -> ActionResult`

Read whether a `TitledPane` is expanded.

`result.value`: `True` or `False`.

---

## Hyperlink Actions

### `client.get_visited(**selector) -> ActionResult`

Read whether a `Hyperlink` has been visited.

`result.value`: `True` or `False`.

---

## TreeTableView Actions

### `client.select_tree_table_row(value, column=None, **selector) -> ActionResult`

Select a row in a `TreeTableView` by matching cell content.

Parameters:
- `value: str` — cell text to match
- `column: str | None` — optional column name to restrict the match

### `client.get_tree_table_cell(row, column, **selector) -> ActionResult`

Read the text of a specific cell.

Parameters:
- `row: str` — row identifier (item value)
- `column: str` — column name

### `client.expand_tree_table_item(value, **selector) -> ActionResult`

Expand a tree item in a `TreeTableView` by matching its cell value.

### `client.collapse_tree_table_item(value, **selector) -> ActionResult`

Collapse a tree item in a `TreeTableView` by matching its cell value.

### `client.get_tree_table_expanded(value, **selector) -> ActionResult`

Read whether a tree item is expanded.

`result.value`: `True` or `False`.

Fetch the screenshot payload from the agent.

Current implementation note:
- In the demo/reference path, screenshot bytes may be OCR-friendly text fixture content rather than a real PNG bitmap.

### `client.ocr(image: bytes) -> list[dict[str, Any]]`

Run the configured OCR engine against image bytes.

Current return fields:
- `text`
- `confidence`
- `bounds`

### `client.vision_match(template: bytes) -> dict[str, Any]`

Run the configured vision engine against the latest screenshot.

Current return fields:
- `matched`
- `confidence`
- `bounds`

### `client.action_history() -> list[ActionLogEntry]`

Return the recorded action log for the current client session.

The list contains completed action results only.

## Node State Queries

### `client.is_visible(**selector) -> bool`

Return `True` if the matched node is currently visible.

Returns `False` if no node matches the selector (does not raise).

```python
if client.is_visible(id="submitButton"):
    client.click(id="submitButton")
```

### `client.is_enabled(**selector) -> bool`

Return `True` if the matched node is currently enabled (not disabled).

Returns `False` if no node matches the selector (does not raise).

```python
assert client.is_enabled(id="loginButton"), "Login button should be enabled"
assert not client.is_enabled(id="__no_such_node__")  # always False
```

## Wait Conditions

Poll-based helpers that block until a UI state condition is met or raise `TimeoutError` if the timeout expires. All methods accept `timeout` (seconds, default `5.0`) and `interval` (poll period in seconds, default `0.2`).

### `client.wait_for_text(id, expected, timeout=5.0, interval=0.2) -> None`

Block until the text of node `id` equals `expected`.

```python
client.click(id="loadButton")
client.wait_for_text("statusLabel", "Done", timeout=10.0)
```

### `client.wait_for_visible(id, timeout=5.0, interval=0.2) -> None`

Block until node `id` is visible (`is_visible` returns `True`).

```python
client.wait_for_visible("resultPanel")
```

### `client.wait_for_enabled(id, timeout=5.0, interval=0.2) -> None`

Block until node `id` is enabled (`is_enabled` returns `True`).

```python
client.wait_for_enabled("submitButton")
```

### `client.wait_for_node(id, timeout=5.0, interval=0.2) -> None`

Block until a node with `fxId == id` appears in node discovery.

```python
client.wait_for_node("dynamicWidget", timeout=3.0)
```

### `client.wait_for_value(id, expected, timeout=5.0, interval=0.2) -> None`

Alias for `wait_for_text`. Provided for readability in value-assertion contexts.

```python
client.wait_for_value("totalField", "42")
```

## Close App

### `client.close_app() -> ActionResult`

Trigger graceful shutdown of the JavaFX application and the agent HTTP server.

Internally schedules `System.exit(0)` after a 200 ms delay (to allow the HTTP response to flush), then terminates the full JVM. The agent also installs an `omniui-exit-monitor` daemon thread at startup that automatically calls `System.exit(0)` when the JavaFX Application Thread exits — so closing the window via the OS [x] button also terminates the process cleanly.

This should be the **last call** in a session. Subsequent API calls will raise connection errors as the JVM exits.

```python
# Clean up after test suite
client.close_app()
```

## Selectors

The currently supported selector surface is:

```python
{
    "id": "...",
    "text": "...",
    "type": "...",
}
```

Typical usage:

```python
client.click(id="loginButton")
client.click(text="Login", type="Button")
```

Additional current behavior:
- `click(..., template=b"...")` is accepted by the internal fallback pipeline for vision matching

## Result Models

## `Selector`

Fields:
- `id: str | None`
- `text: str | None`
- `type: str | None`

## `ResolvedElement`

Fields:
- `tier: str`
- `target_ref: str`
- `selector_used: Selector`
- `matched_attributes: dict[str, Any]`
- `confidence: float | None`
- `debug_context: dict[str, Any]`

Tier values currently used:
- `javafx`
- `ocr`
- `vision`

## `ActionTrace`

Fields:
- `selector: Selector`
- `attempted_tiers: list[str]`
- `resolved_tier: str | None`
- `confidence: float | None`
- `details: dict[str, Any]`

Typical attempted tier sequences:
- `["javafx"]`
- `["javafx", "refresh"]`
- `["javafx", "refresh", "ocr"]`
- `["javafx", "refresh", "ocr", "vision"]`

## `ActionResult`

Fields:
- `ok: bool`
- `trace: ActionTrace`
- `resolved: ResolvedElement | None`
- `value: Any`

## `ActionLogEntry`

Fields:
- `action: str`
- `timestamp: datetime`
- `result: ActionResult`

## OCR and Vision Provider Interfaces

Current default OCR provider:
- `SimpleOcrEngine`

Method:
- `read(image: bytes) -> list[OcrMatch]`

Current default vision provider:
- `SimpleVisionEngine`

Method:
- `match(image: bytes, template: bytes) -> VisionMatch`

These are deterministic placeholder implementations intended to keep Phase 1 executable before production OCR or vision libraries are integrated.

## Examples

### Direct JavaFX login flow

```python
from omniui import OmniUI

client = OmniUI.connect(app_name="LoginDemo")

client.click(id="username")
client.type("admin", id="username")

client.click(id="password")
client.type("1234", id="password")

client.click(id="loginButton")
client.verify_text(id="status", expected="Success")
```

### OCR fallback click

```python
client.click(text="Login")
```

### Full demo suite

Run all included component demos end-to-end:

```bash
python demo/python/run_all.py
```

All demos connect to the same running JavaFX app with the agent enabled.

### Menu navigation

```python
# MenuBar: File > New
client.navigate_menu("File/New", id="demoMenuBar")

# ContextMenu: right-click a node then pick an item
client.right_click(id="someNode")
client.click_menu_item("Copy")
```

### DatePicker

```python
# Direct set (preferred)
client.set_date("2025-09-15", id="demoPicker")
```

### Alert dialog

```python
client.click(id="infoAlertButton")
result = client.get_dialog()
print(result.value["content"])
client.dismiss_dialog("OK")
```

### TreeTableView

```python
client.expand_tree_table_item("Engineering", id="demoTreeTable")
client.select_tree_table_row("Alice", id="demoTreeTable")
cell = client.get_tree_table_cell("Alice", "Name", id="demoTreeTable")
```

## Current Limitations

- No formal generated API reference yet; this document is manually maintained.
- Fallback click currently resolves and records bounds, but does not issue a real OS click.
- `type()` currently depends on JavaFX direct interaction and does not have OCR/vision fallback.
- `find()` normalizes selectors only; it does not resolve them against the application.
