## MODIFIED Requirements

### Requirement: Advanced JavaFX demo coverage
The system SHALL provide reference JavaFX demo scenarios that exercise advanced control patterns beyond the basic login flow, including `ComboBox`, `ListView`, `TreeView`, `TableView`, `ContextMenu`, `MenuBar`, `DatePicker`, `Dialog`, and `Alert`.

#### Scenario: Open advanced demo scenarios
- **WHEN** a user launches the reference JavaFX demo application
- **THEN** the application exposes identifiable scenarios for all advanced controls including ContextMenu, MenuBar, DatePicker, Dialog, and Alert, rather than only the basic login flow and selection controls

## ADDED Requirements

### Requirement: Popup and overlay control demo scenes
The system SHALL provide dedicated demo scenes for each popup-backed and overlay-driven control type — ContextMenu, MenuBar, DatePicker, Dialog, and Alert — with stable, uniquely named nodes and deterministic sample data.

#### Scenario: Open ContextMenu demo scene
- **WHEN** the user navigates to the ContextMenu demo scene
- **THEN** the scene presents a labeled node with a registered ContextMenu containing stable single-level items and at least one multi-level submenu path with uniquely named items

#### Scenario: Open MenuBar demo scene
- **WHEN** the user navigates to the MenuBar demo scene
- **THEN** the scene presents a MenuBar with at least two top-level menus, each containing stable single-level items and at least one multi-level submenu path with uniquely named items

#### Scenario: Open DatePicker demo scene
- **WHEN** the user navigates to the DatePicker demo scene
- **THEN** the scene presents a DatePicker with a stable initial date value and a labeled result node that updates to reflect the selected date

#### Scenario: Open Dialog demo scene
- **WHEN** the user navigates to the Dialog demo scene
- **THEN** the scene presents a trigger control that opens a standard JavaFX Dialog with a stable title, header text, content text, and named OK and Cancel buttons

#### Scenario: Open Alert demo scene
- **WHEN** the user navigates to the Alert demo scene
- **THEN** the scene presents trigger controls for each AlertType (INFORMATION, CONFIRMATION, WARNING, ERROR), each producing an Alert with a stable, uniquely identifiable content message

### Requirement: Stable demo data for popup and overlay controls
The system SHALL use deterministic, human-readable sample data in all popup and overlay demo scenes so that structural discovery and automation assertions remain interpretable and repeatable.

#### Scenario: Inspect popup and overlay demo datasets
- **WHEN** the user or Python client inspects the popup and overlay demo scenes
- **THEN** all menu item labels, dialog texts, alert messages, and date values are stable, uniquely named, and suitable for selector-based automation

### Requirement: Runnable popup and overlay Python demos
The system SHALL provide runnable Python demo scripts that exercise ContextMenu, MenuBar, DatePicker, Dialog, and Alert automation and print enough output to verify discovery and interaction behavior.

#### Scenario: Run popup and overlay control demo script
- **WHEN** a user executes the Python demo for popup and overlay controls against an agent-enabled application
- **THEN** the script exercises right-click and menu item selection, menu bar navigation, date selection, dialog dismissal, and alert interaction, and reports the resulting action trace for each control type
