## Context

JavaFX `TableView` stores data in an `ObservableList` of row objects and renders them through `TableColumn` instances. Accessing a cell requires traversing the virtual flow's cell factory, which is complex via reflection. The existing agent already uses `ReflectiveJavaFxSupport` to call methods by name, making it well-suited for the new table actions.

There are two layers to interact with: the data model (`TableView.getItems()` → row object → property getter) and the visual cells (`VirtualFlow → IndexedCell`). For read and click, both approaches can work; for edit, the visual cell must be activated via double-click.

## Goals / Non-Goals

**Goals:**
- `get_cell` — read the string representation of a cell's value via the data model
- `click_cell` — fire a single mouse click on the visual cell at (row, column)
- `edit_cell` — start editing via `TableView.edit(row, column)` + commit with Enter
- `sort_column` — click a column header and return the resulting sort type

**Non-Goals:**
- Multi-row selection (`select_rows`)
- Drag-and-drop within table
- Virtual scrolling pagination (only visible rows targeted)
- Tree-TableView (separate future item)

## Decisions

**D1: Data access strategy for `get_cell`**
Use `TableView.getColumns().get(colIndex)` → `TableColumn.getCellObservableValue(rowIndex)` → `.getValue().toString()`. This reads from the model, not the rendered cell, so it works regardless of scroll position.
Alternative (visual scraping via `VirtualFlow`) is fragile and scroll-dependent.

**D2: Click strategy for `click_cell`**
Scroll to the cell with `TableView.scrollTo(row)` + `TableView.scrollToColumn(column)`, then find the visual cell in `VirtualFlow` and fire a `MouseEvent.MOUSE_CLICKED` on it.
Alternative (fire directly on TableView with computed coordinates) is unreliable on HiDPI.

**D3: Edit commit strategy**
Call `TableView.edit(row, column)` on the FX thread to enter edit mode, then fire `press_key("Enter")` to commit. This avoids the complexity of finding the inline editor node.

**D4: Row/column index convention**
Zero-based, consistent with `ObservableList` and `TableColumn` APIs. Documented in spec.

## Risks / Trade-offs

- [VirtualFlow internals are private API] → Use `ReflectiveJavaFxSupport.invoke` with `setAccessible`; tested against JavaFX 17+
- [edit_cell depends on cell being editable] → Return `ok=False` with `reason="not_editable"` if `TableColumn.isEditable()` is false
- [get_cell on large tables] → Model-based access is O(1), no risk
