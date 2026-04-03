## ADDED Requirements

### Requirement: TabPane tab listing
The system SHALL support listing all tabs in a `TabPane` node, returning each
tab's title text and disabled state.

#### Scenario: List tabs of a TabPane
- **WHEN** the automation client calls `get_tabs(id="<tabPaneId>")`
- **THEN** the system returns a list of objects each containing `text` (tab title)
  and `disabled` (boolean) for every tab in the TabPane

### Requirement: TabPane tab selection
The system SHALL support selecting a tab in a `TabPane` by its title text.

#### Scenario: Select tab by title
- **WHEN** the automation client calls `select_tab(id="<tabPaneId>", tab="<title>")`
  and a tab with that title exists
- **THEN** the TabPane's selection model selects that tab and it becomes the
  active tab

#### Scenario: Fail when tab title not found
- **WHEN** the automation client calls `select_tab` with a title that matches
  no tab in the TabPane
- **THEN** the system returns a failure result with reason `tab_not_found`
