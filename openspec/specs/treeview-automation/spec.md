## Purpose

Define automation behavior for JavaFX TreeView controls.

## Requirements

### Requirement: TreeView item selection
A TreeView automation implementation SHALL support selecting a TreeItem by its value string. Parent nodes are automatically expanded to make the target item visible before selection.

#### Scenario: Select top-level item
- **WHEN** `select(id="<id>", value="X")` is called and `"X"` is a root-level item
- **THEN** that item becomes selected

#### Scenario: Select nested item expands parents
- **WHEN** `select(id="<id>", value="X")` is called and `"X"` is a nested item
- **THEN** all ancestor nodes are expanded and `"X"` becomes selected

#### Scenario: Select non-existent item fails
- **WHEN** `select` is called with a value that is not found anywhere in the tree
- **THEN** returns a failure result with `reason="select_not_supported"`
