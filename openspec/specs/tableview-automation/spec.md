## Purpose

Define automation behavior for JavaFX TableView controls.

## Requirements

### Requirement: TableView row selection
A TableView automation implementation SHALL support selecting a table row by matching a cell value. An optional `column` parameter narrows the search to a specific column.

#### Scenario: Select row by cell value
- **WHEN** `select(id="<id>", value="X")` is called
- **THEN** the row containing a cell with value `"X"` becomes selected

#### Scenario: Select row with column hint
- **WHEN** `select(id="<id>", value="X", column="Name")` is called
- **THEN** the row where the `Name` column equals `"X"` is selected

#### Scenario: Select non-existent value fails
- **WHEN** `select` is called with a value not found in any row
- **THEN** returns a failure result with `reason="select_not_supported"`
