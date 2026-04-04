## Context

`TreeTableView<S>` is a JavaFX control that combines a hierarchical tree on the left column with additional data columns to the right. Each row is a `TreeItem<S>`, and each column is a `TreeTableColumn<S, T>`. Accessing cell values requires walking the `TreeItem` tree recursively, similar to `TreeView`, while reading specific column data requires iterating `getColumns()`.

Currently the framework supports `TreeView` (hierarchy, single value per node) and `TableView` (flat rows, multi-column). Neither covers `TreeTableView`, which adds a vertical tree axis to the tabular model.

## Goals / Non-Goals

**Goals:**
- Support selecting a `TreeTableView` row by matching a cell value (any column, or a named column)
- Support reading a cell value from a `TreeTableView` row by row-index + column name
- Support expanding and collapsing `TreeItem` nodes inside a `TreeTableView`
- Support reading expansion state of a `TreeItem` row
- Provide Python engine methods and a demo section + script

**Non-Goals:**
- Multi-selection (only single-row selection per action call)
- Editing cells in-place (`TreeTableCell` commit)
- Sorting or filtering via column headers
- Drag-and-drop row reordering

## Decisions

### 決策 1：action 命名與映射
| Action | Java method | Notes |
|---|---|---|
| `select_tree_table_row` | `selectTreeTableRow(node, value, column)` | Mirror of `selectTableRow` |
| `get_tree_table_cell` | `getTreeTableCell(node, rowValue, column)` | Read cell by matching row identity |
| `expand_tree_table_item` | `safeInvoke(treeItem, "setExpanded", true)` | Same pattern as Accordion |
| `collapse_tree_table_item` | `safeInvoke(treeItem, "setExpanded", false)` | Same pattern as Accordion |
| `get_tree_table_expanded` | `safeInvoke(treeItem, "isExpanded")` | Same pattern as Accordion |

`select_tree_table_row` and `expand/collapse` actions operate on the `TreeTableView` node (found by selector), then locate the correct `TreeItem` by walking the tree. This is consistent with how `selectTreeItem` works in `TreeView`.

### 決策 2：row lookup strategy
Walk the `TreeItem` tree recursively using `getChildren()`. For each `TreeItem`, call `getValue()` to get the backing object, then call `toString()` on it (or use column cell value factories via reflection if a column name is specified). Fall back to `select_not_supported` if not found.

### 決策 3：`get_tree_table_cell` selector
The action takes the `TreeTableView` node (by id), plus a `row` string (matched by first-column value) and `column` string (column header text). Returns the string value of the cell.

### 決策 4：expand/collapse targets the TreeTableView node by selector, then locates TreeItem
The `expand_tree_table_item` action selector resolves the `TreeTableView` node, then a `value` payload identifies the `TreeItem` to expand/collapse. This avoids needing separate `fx:id` on every `TreeItem`.

## Risks / Trade-offs

- **[風險] TreeItem generic type** → `getValue()` returns `Object`; rely on `toString()` for matching, consistent with how `TreeView` select already works
- **[風險] getColumns() may be nested (column groups)** → Phase 1: only support flat (non-grouped) columns; nested column groups are a Non-Goal
- **[風險] Cell value factory may not be a simple string** → Use `TreeTableColumn.getCellObservableValue(item)` via reflection to get the actual displayed value
