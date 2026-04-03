## Why

`TreeTableView` combines hierarchical tree structure with multi-column tabular display, and is widely used in JavaFX applications for file browsers, org charts, and nested data grids. The automation framework currently supports `TreeView` (single-column hierarchy) and `TableView` (flat rows), but has no support for `TreeTableView`, leaving a significant gap for real-world app automation.

## What Changes

- Add `select_tree_table_row` action in Java agent to select a row in a `TreeTableView` by matching a cell value, with optional column narrowing
- Add `get_tree_table_row` action to read a cell value from a specific row/column
- Add `expand_tree_table_item` / `collapse_tree_table_item` actions to control tree node expansion
- Add `get_tree_table_expanded` action to read expansion state of a tree row
- Expose all four actions as Python engine methods
- Add a `TreeTableView` demo section in `LoginDemoApp.java` with sample hierarchical data
- Add `treetableview_demo.py` Python demo script

## Capabilities

### New Capabilities
- `treetableview-automation`: Defines automation behavior for JavaFX TreeTableView — row selection, cell value read, tree node expand/collapse and expansion-state read

### Modified Capabilities
- `javafx-automation-core`: Add TreeTableView as a supported automation target with its four actions
- `advanced-javafx-demo-scenarios`: Add TreeTableView demo section and Python script scenario

## Impact

- `java-agent/.../ReflectiveJavaFxTarget.java`: New action cases + helper methods for TreeTableView
- `omniui/core/engine.py`: Four new public methods
- `demo/javafx-login-app/.../LoginDemoApp.java`: New TreeTableView demo section
- `demo/python/`: New `treetableview_demo.py`, updated `run_all.py`
