## ADDED Requirements

### Requirement: get_cell reads a cell value by row and column index
The system SHALL provide `get_cell(id, row, column)` on `OmniUIClient` that returns the string value of the cell at zero-based `row` and `column` indices in the `TableView` identified by `id`. The value SHALL be obtained from the data model via `TableColumn.getCellObservableValue`.

#### Scenario: Read a cell value
- **WHEN** `client.get_cell(id="myTable", row=0, column=1)` is called
- **THEN** `ActionResult.ok` is `True` and `ActionResult.value` contains the cell's string value

#### Scenario: Row index out of bounds
- **WHEN** `row` is greater than or equal to the number of items in the table
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="row_out_of_bounds"`

#### Scenario: Column index out of bounds
- **WHEN** `column` is greater than or equal to the number of columns
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="column_out_of_bounds"`

### Requirement: click_cell fires a mouse click on a visual table cell
The system SHALL provide `click_cell(id, row, column)` on `OmniUIClient` that scrolls the `TableView` to the target cell and fires a `MouseEvent.MOUSE_CLICKED` (single click, PRIMARY button) on the visual cell node.

#### Scenario: Click a cell
- **WHEN** `client.click_cell(id="myTable", row=2, column=0)` is called
- **THEN** a single primary mouse click is fired on the visual cell at row 2, column 0
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Row/column indices are zero-based
- **WHEN** `client.click_cell(id="myTable", row=0, column=0)` is called
- **THEN** the first cell in the first row and first column receives the click
