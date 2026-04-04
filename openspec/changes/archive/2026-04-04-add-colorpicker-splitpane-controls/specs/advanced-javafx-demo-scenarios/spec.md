## MODIFIED Requirements

### Requirement: Advanced JavaFX demo coverage
The system SHALL provide reference JavaFX demo scenarios that exercise advanced control patterns beyond the basic login flow, including `ComboBox`, `ListView`, `TreeView`, `TableView`, grid-oriented layouts, `ContextMenu`, `MenuBar`, `DatePicker`, `Dialog`, `Alert`, `ColorPicker`, and `SplitPane`.

#### Scenario: Open advanced demo scenarios
- **WHEN** a user launches the reference JavaFX demo application
- **THEN** the application exposes identifiable scenarios for all advanced controls including ContextMenu, MenuBar, DatePicker, Dialog, Alert, ColorPicker, and SplitPane, rather than only the basic login flow and selection controls

## ADDED Requirements

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
