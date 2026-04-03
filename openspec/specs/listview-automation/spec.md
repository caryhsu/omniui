## Purpose

Define automation behavior for JavaFX ListView controls.

## Requirements

### Requirement: ListView item selection
A ListView automation implementation SHALL support selecting an item in a ListView by its string value. Both single and multiple selection models are supported.

#### Scenario: Select existing item
- **WHEN** `select(id="<id>", value="X")` is called and `"X"` is present in the ListView items
- **THEN** the item `"X"` becomes selected

#### Scenario: Select non-existent item fails with item_not_found
- **WHEN** `select` is called with a value that is not in the ListView item list
- **THEN** returns a failure result with `reason="item_not_found"`
