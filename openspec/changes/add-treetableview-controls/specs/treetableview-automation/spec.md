## ADDED Requirements

### Requirement: TreeTableView row selection by cell value
The system SHALL support selecting a row in a `TreeTableView` by matching a cell value string. An optional `column` parameter narrows the search to a specific column by its header text.

#### Scenario: Select row by any-column match
- **WHEN** the automation client calls `select_tree_table_row(id="<id>", value="X")` and a row contains "X" in any column
- **THEN** that row becomes selected in the TreeTableView's SelectionModel

#### Scenario: Select row with column hint
- **WHEN** the automation client calls `select_tree_table_row(id="<id>", value="X", column="Name")` and a row has "X" in the "Name" column
- **THEN** that row becomes selected

#### Scenario: Select non-existent value fails
- **WHEN** the automation client calls `select_tree_table_row` with a value not found in any row
- **THEN** the action returns failure with `reason="select_not_supported"`

### Requirement: TreeTableView cell value read
The system SHALL support reading the string value of a specific cell in a `TreeTableView` by specifying the row (matched by first-column value) and the column (by header text).

#### Scenario: Read cell value
- **WHEN** the automation client calls `get_tree_table_cell(id="<id>", row="X", column="Age")` and a row identified by "X" exists
- **THEN** the system returns the string value of the "Age" cell for that row

#### Scenario: Read cell for unknown row fails
- **WHEN** the automation client calls `get_tree_table_cell` with a row value not found in the tree
- **THEN** the action returns failure with `reason="selector_not_found"`

### Requirement: TreeTableView tree node expand and collapse
The system SHALL support expanding and collapsing a `TreeItem` node inside a `TreeTableView` by calling `setExpanded(boolean)` on the matched item.

#### Scenario: Expand a tree item
- **WHEN** the automation client calls `expand_tree_table_item(id="<id>", value="X")` on a collapsed TreeItem
- **THEN** `treeItem.setExpanded(true)` is called and the item's children become visible

#### Scenario: Collapse a tree item
- **WHEN** the automation client calls `collapse_tree_table_item(id="<id>", value="X")` on an expanded TreeItem
- **THEN** `treeItem.setExpanded(false)` is called and the item's children are hidden

### Requirement: TreeTableView tree node expansion state read
The system SHALL support reading the expansion state of a `TreeItem` inside a `TreeTableView`.

#### Scenario: Read expanded state of a tree item
- **WHEN** the automation client calls `get_tree_table_expanded(id="<id>", value="X")`
- **THEN** the system returns `true` if the matching TreeItem is expanded, `false` if collapsed
