## ADDED Requirements

### Requirement: ColorPicker direct interaction
The system SHALL support `set_color` and `get_color` actions for `ColorPicker` nodes, and `open_colorpicker` / `dismiss_colorpicker` for popup interaction, consistent with the DatePicker action pattern.

#### Scenario: Set color directly on ColorPicker
- **WHEN** a script calls `set_color("#FF5733", id="demoPicker")`
- **THEN** the agent sets the `ColorPicker` value directly via `setValue(Color)` and returns `ok: true`

#### Scenario: Read color from ColorPicker
- **WHEN** a script calls `get_color(id="demoPicker")`
- **THEN** the agent returns the current color as a `#RRGGBB` hex string with `ok: true`

### Requirement: SplitPane divider control
The system SHALL support `get_divider_positions` and `set_divider_position` actions for `SplitPane` nodes, allowing automation scripts to read and programmatically adjust panel sizes.

#### Scenario: Read SplitPane divider positions
- **WHEN** a script calls `get_divider_positions(id="demoSplitPane")`
- **THEN** the agent returns all divider positions as a list of floats in `[0.0, 1.0]`

#### Scenario: Set SplitPane divider position
- **WHEN** a script calls `set_divider_position(0, 0.3, id="demoSplitPane")`
- **THEN** the agent moves the first divider to `0.3` and returns `ok: true`
