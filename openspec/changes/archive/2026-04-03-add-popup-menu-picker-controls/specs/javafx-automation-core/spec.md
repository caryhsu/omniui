## ADDED Requirements

### Requirement: Overlay window node discovery
The system SHALL enumerate nodes from all open overlay windows — including `PopupWindow` instances (ContextMenu, MenuBar popup, DatePicker popup) and Dialog or Alert `Stage` instances — in addition to the primary scene graph, when a query is made while an overlay is visible.

#### Scenario: Discover nodes in a visible ContextMenu overlay
- **WHEN** a ContextMenu is open and the Python client calls `get_nodes()`
- **THEN** the system returns node records for the ContextMenu's `MenuItem` nodes from the overlay window in addition to primary scene nodes

#### Scenario: Discover nodes in an open Dialog
- **WHEN** a modal Dialog is visible and the Python client calls `get_nodes()`
- **THEN** the system returns node records from the `DialogPane` scene including title node, content node, and button nodes

#### Scenario: Discover nodes in an open DatePicker popup
- **WHEN** a DatePicker calendar popup is open and the Python client calls `get_nodes()`
- **THEN** the system returns node records for the calendar popup's date cells and navigation buttons in addition to primary scene nodes

### Requirement: Wait-for-overlay readiness
The system SHALL provide a mechanism that polls the open window list after a popup-triggering action until a new overlay window matching the expected type appears, before returning control to the Python client.

#### Scenario: Wait for ContextMenu popup to appear after right-click
- **WHEN** the Python client dispatches a right-click that triggers a ContextMenu
- **THEN** the system waits until a ContextMenu `PopupWindow` appears in the open window list before reporting success

#### Scenario: Report timeout when overlay does not appear
- **WHEN** the Python client dispatches a right-click on a node with no ContextMenu and waits for an overlay popup
- **THEN** the system reports a timeout error after the configured wait duration without returning partial results
