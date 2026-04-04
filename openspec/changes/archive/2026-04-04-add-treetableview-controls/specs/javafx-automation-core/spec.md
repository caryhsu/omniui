## ADDED Requirements

### Requirement: TreeTableView as automation target
The system SHALL support `TreeTableView` as an automation target with five actions: `select_tree_table_row`, `get_tree_table_cell`, `expand_tree_table_item`, `collapse_tree_table_item`, and `get_tree_table_expanded`.

#### Scenario: select_tree_table_row routes to SelectionModel
- **WHEN** `perform("select_tree_table_row", selector, payload)` is called on a TreeTableView node
- **THEN** the matching TreeItem's row is selected via `TreeTableView.getSelectionModel().select(index)`

#### Scenario: expand_tree_table_item routes to TreeItem.setExpanded(true)
- **WHEN** `perform("expand_tree_table_item", selector, payload)` is called
- **THEN** the matching TreeItem's `setExpanded(true)` is called on the FX thread

#### Scenario: collapse_tree_table_item routes to TreeItem.setExpanded(false)
- **WHEN** `perform("collapse_tree_table_item", selector, payload)` is called
- **THEN** the matching TreeItem's `setExpanded(false)` is called on the FX thread
