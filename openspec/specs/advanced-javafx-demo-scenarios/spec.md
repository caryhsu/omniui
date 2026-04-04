## Purpose

Define the reference advanced JavaFX demo scenarios used to validate control discovery and interaction behavior beyond the basic login flow.

## Requirements

### Requirement: Advanced JavaFX demo coverage
The system SHALL provide reference JavaFX demo scenarios that exercise advanced control patterns beyond the basic login flow, including `ComboBox`, `ListView`, `TreeView`, `TableView`, grid-oriented layouts, `ContextMenu`, `MenuBar`, `DatePicker`, `Dialog`, `Alert`, `ColorPicker`, and `SplitPane`.

#### Scenario: Open advanced demo scenarios
- **WHEN** a user launches the reference JavaFX demo application
- **THEN** the application exposes identifiable scenarios for all advanced controls including ContextMenu, MenuBar, DatePicker, Dialog, Alert, ColorPicker, and SplitPane, rather than only the basic login flow and selection controls

### Requirement: Stable demo data for advanced controls
The system SHALL use deterministic, human-readable sample data in advanced demo scenarios so structural discovery and automation assertions remain interpretable.

#### Scenario: Inspect advanced control dataset
- **WHEN** the user or Python client inspects advanced scenarios
- **THEN** visible options, rows, items, and tree labels are stable, uniquely named, and suitable for selector-based automation

### Requirement: Runnable advanced Python demos
The system SHALL provide runnable Python demos that exercise the advanced JavaFX scenarios and print enough output to inspect discovery and action behavior.

#### Scenario: Run advanced control demo script
- **WHEN** a user executes a Python demo for advanced JavaFX scenarios against an agent-enabled app
- **THEN** the script exercises representative control interactions and reports the resulting discovery or action trace

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

### Requirement: Input and navigation control demo scenes
The demo application SHALL include dedicated demo sections for RadioButton/ToggleButton, Slider/Spinner, ProgressBar, and TabPane controls, with stable `fx:id` attributes and corresponding runnable Python demo scripts.

#### Scenario: New demo sections present in running application
- **WHEN** the demo application is running
- **THEN** `get_nodes()` returns nodes with ids `radioToggleSection`, `sliderSpinnerSection`, `progressSection`, `tabSection`, and their child control nodes

#### Scenario: Input and navigation demo scripts pass
- **WHEN** each new Python demo script is run against the demo application
- **THEN** the script exits with code 0 and prints a success message

### Requirement: Text input control demo scenes
The demo application SHALL include dedicated demo sections for TextArea, PasswordField, and Hyperlink controls, with stable `fx:id` attributes and corresponding runnable Python demo scripts.

#### Scenario: TextArea, PasswordField, Hyperlink sections present
- **WHEN** the demo application is running
- **THEN** nodes with ids `demoTextArea`, `demoPasswordField`, and `demoHyperlink` are discoverable in the scene graph

#### Scenario: Text input demo scripts pass
- **WHEN** `text_area_demo.py`, `password_field_demo.py`, and `hyperlink_demo.py` are run against the demo application
- **THEN** each script exits with code 0 and prints a success message


### Requirement: CheckBox and ChoiceBox demo scenes
The demo application SHALL include dedicated demo sections for CheckBox and ChoiceBox controls, and corresponding Python demo scripts SHALL exercise the full automation API for each control.

#### Scenario: CheckBox demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `checkBoxSection` is visible containing at least three independent CheckBoxes with distinct `fx:id` values

#### Scenario: ChoiceBox demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `choiceBoxSection` is visible containing a ChoiceBox (`id="demoChoiceBox"`) pre-populated with at least three selectable string items

#### Scenario: Python demo scripts pass for CheckBox and ChoiceBox
- **WHEN** `checkbox_demo.py` and `choicebox_demo.py` are run against the demo application
- **THEN** each script exits with code 0 and prints a success message

### Requirement: Accordion and TitledPane demo scene
The demo application SHALL include a dedicated demo section for Accordion and TitledPane controls, with a corresponding Python demo script that exercises expand, collapse, and state-read operations.

#### Scenario: Accordion demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `accordionSection` is visible containing an Accordion (`id="demoAccordion"`) with at least three TitledPanes with distinct `fx:id` values

#### Scenario: Python accordion demo script passes
- **WHEN** `accordion_demo.py` is run against the demo application
- **THEN** the script exits with code 0 and prints a success message

### Requirement: TreeTableView demo section
The demo application SHALL include a dedicated section for TreeTableView with hierarchical sample data and a corresponding Python demo script.

#### Scenario: TreeTableView demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `treeTableSection` is visible, containing a `TreeTableView` with id `demoTreeTable` with at least two columns and at least two levels of hierarchy

#### Scenario: Python TreeTableView demo script passes
- **WHEN** `treetableview_demo.py` is run against the demo application
- **THEN** the script exits with code 0 and prints a success message

### Requirement: ColorPicker demo scene
The system SHALL provide a dedicated demo scene for `ColorPicker` with a named `ColorPicker` node, a label that displays the currently selected color, and a button that triggers a color change, enabling both direct-set and popup-based automation testing.

#### Scenario: Open ColorPicker demo scene
- **WHEN** the user navigates to the ColorPicker demo section
- **THEN** the scene presents a `ColorPicker` with `fx:id="demoPicker"`, a label with `fx:id="colorResult"` showing the selected color as `#RRGGBB`, and a button that resets the picker to a default color

#### Scenario: Direct color set reflected in label
- **WHEN** `set_color("#1E90FF", id="demoPicker")` is called
- **THEN** the label `colorResult` updates to `Selected: #1e90ff`

### Requirement: SplitPane demo scene
The system SHALL provide a dedicated demo scene for `SplitPane` with a named `SplitPane` node containing at least two panels and a label that displays the current divider positions.

#### Scenario: Open SplitPane demo scene
- **WHEN** the user navigates to the SplitPane demo section
- **THEN** the scene presents a `SplitPane` with `fx:id="demoSplitPane"` containing two labeled panels and a label with `fx:id="dividerResult"` showing the current divider position

#### Scenario: Set divider position reflected in label
- **WHEN** `set_divider_position(0, 0.3, id="demoSplitPane")` is called
- **THEN** the label `dividerResult` updates to reflect the new position (approximately `0.3`)
