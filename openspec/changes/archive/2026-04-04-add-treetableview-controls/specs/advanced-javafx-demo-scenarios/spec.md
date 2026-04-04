## ADDED Requirements

### Requirement: TreeTableView demo section
The demo application SHALL include a dedicated section for TreeTableView with hierarchical sample data and a corresponding Python demo script.

#### Scenario: TreeTableView demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `treeTableSection` is visible, containing a `TreeTableView` with id `demoTreeTable` with at least two columns and at least two levels of hierarchy

#### Scenario: Python TreeTableView demo script passes
- **WHEN** `treetableview_demo.py` is run against the demo application
- **THEN** the script exits with code 0 and prints a success message
