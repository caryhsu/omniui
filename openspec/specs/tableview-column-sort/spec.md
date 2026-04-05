## ADDED Requirements

### Requirement: sort_column clicks a column header to sort the TableView
The system SHALL provide `sort_column(id, column, direction)` on `OmniUIClient` that clicks the column header at zero-based index `column` in the `TableView` identified by `id`, cycling the sort until the sort type matches `direction` ("asc" or "desc"). If `direction` is omitted the header is clicked once regardless of resulting sort state.

#### Scenario: Sort ascending
- **WHEN** `client.sort_column(id="myTable", column=0, direction="asc")` is called
- **THEN** the column header is clicked until `TableColumn.getSortType()` returns `ASCENDING`
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Sort descending
- **WHEN** `client.sort_column(id="myTable", column=0, direction="desc")` is called
- **THEN** the column header is clicked until `TableColumn.getSortType()` returns `DESCENDING`
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Sort without direction
- **WHEN** `client.sort_column(id="myTable", column=1)` is called with no `direction`
- **THEN** the column header receives one click
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Column index out of bounds
- **WHEN** `column` is greater than or equal to the number of columns
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="column_out_of_bounds"`
