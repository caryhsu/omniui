## Purpose

Define automation behavior for JavaFX ComboBox controls.

## Requirements

### Requirement: ComboBox item selection
A ComboBox automation implementation SHALL support selecting an item from a ComboBox's item list by value string.

#### Scenario: Select existing item
- **WHEN** `select(id="<id>", value="X")` is called and `"X"` is present in the ComboBox items
- **THEN** `getValue()` returns `"X"`

#### Scenario: Select non-existent item fails with item_not_found
- **WHEN** `select` is called with a value that is not in the ComboBox item list
- **THEN** returns a failure result with `reason="item_not_found"`

### Requirement: ComboBox value read
A ComboBox automation implementation SHALL support reading the currently selected value by calling `getValue()`.

#### Scenario: Read selected value
- **WHEN** `get_value(id="<id>")` is called
- **THEN** returns the currently selected item as a string
