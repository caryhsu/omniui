## ADDED Requirements

### Requirement: color-app provides ColorPicker automation scenarios
The color-app (port 48106) SHALL provide a standalone JavaFX demo application with the following interactive elements:
- `demoPicker`: a `ColorPicker` control for color selection
- `colorResult`: a `Label` that displays the currently selected color as `Selected: #RRGGBB` whenever the picker value changes
- `resetColorButton`: a `Button` that resets the picker to its default color (`#ffffff`) and clears `colorResult`

#### Scenario: Initial state
- **WHEN** the color-app starts
- **THEN** `demoPicker` is visible and enabled
- **AND** `colorResult` text is empty or shows the default color

#### Scenario: set_color updates colorResult label
- **WHEN** Python calls `set_color("#ff5733", id="demoPicker")`
- **THEN** `colorResult` label text contains `#ff5733`

#### Scenario: get_color reads current picker value
- **WHEN** Python calls `get_color(id="demoPicker")` after setting a color
- **THEN** the returned hex string matches what was set

#### Scenario: reset button restores default state
- **WHEN** Python clicks `resetColorButton`
- **THEN** `colorResult` text is cleared (empty string)
