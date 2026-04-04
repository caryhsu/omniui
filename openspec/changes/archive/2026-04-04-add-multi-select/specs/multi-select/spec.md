## ADDED Requirements

### Requirement: select_multiple selects several items by value
The system SHALL provide a `select_multiple` action that clears the existing selection and selects each item whose `toString()` matches a value in the provided list, using `MultipleSelectionModel.selectIndices()`.

#### Scenario: Select two items in a MULTIPLE-mode ListView
- **WHEN** an automation script calls `client.select_multiple(id="serverList", values=["Alpha", "Gamma"])`
- **THEN** the Java agent finds the indices of "Alpha" and "Gamma" in the list model, calls `clearSelection()` then `selectIndices(alphaIdx, gammaIdx)`, and both items are selected

#### Scenario: Value not found is skipped silently
- **WHEN** an automation script calls `client.select_multiple(id="serverList", values=["Alpha", "NonExistent"])`
- **THEN** only "Alpha" is selected; "NonExistent" is skipped without error

### Requirement: get_selected_items returns all selected values
The system SHALL provide a `get_selected_items` action that returns a `List<String>` of `toString()` representations of all currently selected items in a `ListView` or `TableView`.

#### Scenario: Read multi-selection result
- **WHEN** two items are selected in a MULTIPLE-mode ListView and an automation script calls `client.get_selected_items(id="serverList")`
- **THEN** `result.value` is a list containing the string representations of the two selected items

#### Scenario: No selection returns empty list
- **WHEN** no items are selected and an automation script calls `client.get_selected_items(id="serverList")`
- **THEN** `result.value` is an empty list `[]`
