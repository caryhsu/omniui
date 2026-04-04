## Purpose

Define the automation behavior for JavaFX `ColorPicker` controls in OmniUI.

## ADDED Requirements

### Requirement: Direct color set
The system SHALL support a `set_color` action that sets the value of a `ColorPicker` node directly via `setValue(Color)` without opening the popup, accepting a CSS-compatible hex color string.

#### Scenario: Set color by hex string
- **WHEN** a script calls `set_color("#FF5733", id="demoPicker")`
- **THEN** the `ColorPicker` node with `fx:id="demoPicker"` has its value set to the corresponding color and the action returns `ok: true` with the color string in `value`

#### Scenario: Reject invalid color string
- **WHEN** a script calls `set_color("notacolor", id="demoPicker")`
- **THEN** the action returns `ok: false` with `reason: "set_color_failed"`

### Requirement: Color value read
The system SHALL support a `get_color` action that reads the current color value of a `ColorPicker` node and returns it as a `#RRGGBB` hex string.

#### Scenario: Read current color
- **WHEN** a script calls `get_color(id="demoPicker")` and the picker has a color set
- **THEN** the action returns `ok: true` with `value` as a `#RRGGBB` hex string

#### Scenario: Read default color when none explicitly set
- **WHEN** a script calls `get_color(id="demoPicker")` and no color has been set by the test
- **THEN** the action returns `ok: true` with a valid `#RRGGBB` hex string representing the default color

### Requirement: ColorPicker popup open and dismiss
The system SHALL support `open_colorpicker` and `dismiss_colorpicker` actions for popup-based interaction, consistent with the `open_datepicker` / dismiss pattern.

#### Scenario: Open color picker popup
- **WHEN** a script calls `open_colorpicker(id="demoPicker")`
- **THEN** the system clicks the `ColorPicker` node and waits for the color palette overlay to appear, returning `ok: true`

#### Scenario: Dismiss color picker popup
- **WHEN** a color picker popup is open and the script calls `dismiss_colorpicker()`
- **THEN** the popup is closed and the action returns `ok: true`
