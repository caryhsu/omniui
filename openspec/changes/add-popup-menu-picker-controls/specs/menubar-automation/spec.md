## ADDED Requirements

### Requirement: MenuBar top-level menu activation
The system SHALL support opening a top-level MenuBar menu by dispatching a click on the matching Menu item identified by label text or `fx:id`.

#### Scenario: Open top-level menu by label text
- **WHEN** the Python client calls `open_menu(text="File")` on a scene with a MenuBar
- **THEN** the system dispatches a click on the matching top-level Menu item and waits until the menu popup is visible

#### Scenario: Open top-level menu by fx:id
- **WHEN** the Python client calls `open_menu(id="fileMenu")` on a scene with a MenuBar
- **THEN** the system dispatches a click on the Menu item with the matching `fx:id` and waits until the menu popup is visible

### Requirement: MenuBar single-level item selection
The system SHALL support selecting a visible MenuItem from an open top-level MenuBar menu by label text or `fx:id`.

#### Scenario: Click a single-level MenuItem by text in an open menu
- **WHEN** a top-level MenuBar menu is open and the Python client calls `click_menu_item(text="Open")`
- **THEN** the system locates the matching MenuItem in the visible menu popup and dispatches a click event on it

#### Scenario: Click a single-level MenuItem by fx:id in an open menu
- **WHEN** a top-level MenuBar menu is open and the Python client calls `click_menu_item(id="openFileItem")`
- **THEN** the system locates the MenuItem with the matching `fx:id` in the visible menu popup and dispatches a click event on it

### Requirement: MenuBar multi-level submenu navigation
The system SHALL support navigating nested submenus within a MenuBar by accepting a full path from the top-level menu header to the final item, and sequentially activating each level via hover dispatch.

#### Scenario: Navigate a two-level MenuBar path
- **WHEN** the Python client calls `navigate_menu(path=["File", "Export", "CSV"])`
- **THEN** the system opens the "File" menu, hovers "Export" to reveal its submenu, waits until the submenu popup is visible, then dispatches a click on "CSV"

#### Scenario: Navigate a three-level MenuBar path
- **WHEN** the Python client calls `navigate_menu(path=["Edit", "Transform", "Case", "Uppercase"])`
- **THEN** the system sequentially opens each submenu level and dispatches a click on the final "Uppercase" item

### Requirement: MenuBar menu dismissal
The system SHALL support dismissing an open MenuBar menu without selecting any item.

#### Scenario: Dismiss open MenuBar menu by pressing Escape
- **WHEN** a MenuBar menu is open and the Python client calls `dismiss_menu()`
- **THEN** the system dispatches an Escape key event and waits until the menu popup window is no longer visible
