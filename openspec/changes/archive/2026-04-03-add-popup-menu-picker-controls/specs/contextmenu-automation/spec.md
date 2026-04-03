## ADDED Requirements

### Requirement: ContextMenu trigger
The system SHALL support triggering a ContextMenu on a target JavaFX node by dispatching a right-click event through the JavaFX runtime without OS-level pointer movement.

#### Scenario: Open ContextMenu via right-click on target node
- **WHEN** the Python client calls `right_click(selector)` on a node that has a ContextMenu registered
- **THEN** the system dispatches a right-click event on the resolved node and waits until the ContextMenu popup window is visible before returning

#### Scenario: Report error when right-click target has no ContextMenu
- **WHEN** the Python client calls `right_click(selector)` on a node that has no ContextMenu registered and the popup does not appear within the configured timeout
- **THEN** the system reports a timeout error without returning partial results

### Requirement: ContextMenu single-level item selection
The system SHALL support selecting a visible ContextMenu item by matching its label text or `fx:id` after the ContextMenu is open.

#### Scenario: Click a single-level MenuItem by label text
- **WHEN** the ContextMenu is open and the Python client calls `click_menu_item(text="Delete")`
- **THEN** the system locates the matching MenuItem in the visible ContextMenu overlay and dispatches a click event on it

#### Scenario: Click a single-level MenuItem by fx:id
- **WHEN** the ContextMenu is open and the Python client calls `click_menu_item(id="deleteItem")`
- **THEN** the system locates the MenuItem with the matching `fx:id` in the visible ContextMenu overlay and dispatches a click event on it

### Requirement: ContextMenu multi-level item navigation
The system SHALL support navigating a ContextMenu with nested submenus by accepting a path of item labels and sequentially activating each intermediate level via hover dispatch before clicking the final item.

#### Scenario: Navigate and click a two-level submenu item
- **WHEN** the ContextMenu is open and the Python client calls `click_menu_item(path=["Edit", "Copy As"])`
- **THEN** the system hovers the "Edit" item to reveal its submenu, waits until the submenu popup is visible, then dispatches a click on "Copy As"

#### Scenario: Navigate and click a three-level submenu item
- **WHEN** the ContextMenu is open and the Python client calls `click_menu_item(path=["Format", "Text", "Bold"])`
- **THEN** the system sequentially hovers each intermediate item to reveal the next level and dispatches a click on the final "Bold" item

### Requirement: ContextMenu dismissal
The system SHALL support dismissing an open ContextMenu without selecting any item.

#### Scenario: Dismiss ContextMenu by pressing Escape
- **WHEN** the ContextMenu is open and the Python client calls `dismiss_menu()`
- **THEN** the system dispatches an Escape key event and waits until the ContextMenu popup window is no longer visible
