## ADDED Requirements

### Requirement: edit_cell triggers in-cell editing and commits the value
The system SHALL provide `edit_cell(id, row, column, value)` on `OmniUIClient` that calls `TableView.edit(row, column)` on the FX thread to enter edit mode, types `value` into the inline editor, then presses Enter to commit.

#### Scenario: Edit a cell successfully
- **WHEN** `client.edit_cell(id="myTable", row=1, column=2, value="newValue")` is called on an editable column
- **THEN** the cell enters edit mode, "newValue" is typed, and Enter commits the change
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Column is not editable
- **WHEN** `edit_cell` is called on a column where `TableColumn.isEditable()` returns `false`
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="not_editable"`

#### Scenario: Row index out of bounds
- **WHEN** `row` is greater than or equal to the number of items
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="row_out_of_bounds"`
