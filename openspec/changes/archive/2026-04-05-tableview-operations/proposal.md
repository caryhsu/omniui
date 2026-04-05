## Why

TableView is one of the most common complex widgets in JavaFX applications, but OmniUI currently has no fine-grained API for it. Tests that involve tables must work around the missing primitives, reducing reliability. Providing `get_cell`, `click_cell`, `edit_cell`, and `sort_column` covers the majority of real-world TableView interactions.

## What Changes

- Add `get_cell(id, row, column)` — read the text value of a specific table cell
- Add `click_cell(id, row, column)` — click a specific table cell (row/column by index)
- Add `edit_cell(id, row, column, value)` — double-click to enter edit mode, type a value, commit with Enter
- Add `sort_column(id, column, direction)` — click a column header to sort; verify sort direction

## Capabilities

### New Capabilities

- `tableview-cell-access`: `get_cell` and `click_cell` — positional read and click on TableView cells by zero-based row/column index
- `tableview-cell-edit`: `edit_cell` — trigger in-cell editing via double-click and commit via Enter key
- `tableview-column-sort`: `sort_column` — click a column header to sort ascending/descending and query current sort state

### Modified Capabilities

## Impact

- `java-agent/…/ReflectiveJavaFxTarget.java` — new action cases: `get_cell`, `click_cell`, `edit_cell`, `sort_column`
- `omniui/core/engine.py` — new `OmniUIClient` methods
- `tests/test_client.py` — new test class `TableViewTests`
- `demo/python/core/tableview_demo.py` — new demo using the bundled `TableViewApp`
- `demo/python/run_all.py` — add `tableview_demo` to core group
