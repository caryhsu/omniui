## 1. Java Agent — Cell Access

- [ ] 1.1 Add `get_cell` case in `perform()` first-layer switch → call `doGetCell(payload)`
- [ ] 1.2 Implement `doGetCell(JsonObject payload)`: resolve TableView by fxId, call `getColumns().get(col)`, then `getCellObservableValue(row)`, return string value; handle out-of-bounds with `reason`
- [ ] 1.3 Add `click_cell` case in `performOnFxThread()` second-layer switch → call `doClickCell(node, payload)`
- [ ] 1.4 Implement `doClickCell(Object node, JsonObject payload)`: `scrollTo(row)` + `scrollToColumn(col)`, find visual cell via VirtualFlow reflection, fire `MouseEvent.MOUSE_CLICKED` on it

## 2. Java Agent — Cell Edit & Column Sort

- [ ] 2.1 Add `edit_cell` case in `performOnFxThread()` second-layer switch → call `doEditCell(node, payload)`
- [ ] 2.2 Implement `doEditCell(Object node, JsonObject payload)`: check `isEditable()`, call `edit(row, col)`, then `press_key("Enter")` to commit; return `reason="not_editable"` if column not editable
- [ ] 2.3 Add `sort_column` case in `performOnFxThread()` second-layer switch → call `doSortColumn(node, payload)`
- [ ] 2.4 Implement `doSortColumn(Object node, JsonObject payload)`: get column header node via `getColumns().get(col)`, fire click on header; if `direction` provided, loop until `getSortType()` matches; handle out-of-bounds
- [ ] 2.5 `mvn clean install -pl java-agent -am -q` — confirm no compile errors

## 3. Python Client

- [ ] 3.1 Add `get_cell(self, *, id, row, column)` → `_perform("get_cell", {"id": id}, {"row": row, "column": column})`
- [ ] 3.2 Add `click_cell(self, *, id, row, column)` → `_perform("click_cell", {"id": id}, {"row": row, "column": column})`
- [ ] 3.3 Add `edit_cell(self, *, id, row, column, value)` → `_perform("edit_cell", {"id": id}, {"row": row, "column": column, "value": value})`
- [ ] 3.4 Add `sort_column(self, *, id, column, direction=None)` → `_perform("sort_column", {"id": id}, {"column": column, "direction": direction})`

## 4. Demo App — TableView

- [ ] 4.1 Add a simple `TableViewApp` scene to the existing demo JavaFX app (or create a new launcher) with an editable `TableView` containing at least 3 columns and 3 rows
- [ ] 4.2 Create `demo/python/core/tableview_demo.py`: call `get_cell`, `click_cell`, `edit_cell`, `sort_column`
- [ ] 4.3 Add `tableview_demo` import and call to `demo/python/run_all.py`

## 5. Tests

- [ ] 5.1 Add `TableViewTests` to `tests/test_client.py`: mock HTTP, verify each method sends correct action + payload
- [ ] 5.2 Run `python -m pytest tests/` — confirm all pass

## 6. Wrap-up

- [ ] 6.1 Update `ROADMAP.md` and `ROADMAP.zh-TW.md` — mark all 3 TableView items as `[x]`
- [ ] 6.2 `git commit` + `git push`
