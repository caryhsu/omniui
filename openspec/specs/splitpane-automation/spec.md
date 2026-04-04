## Purpose

Define the automation behavior for JavaFX `SplitPane` controls in OmniUI.

## Requirements

### Requirement: Read divider positions
The system SHALL support a `get_divider_positions` action that reads all divider positions of a `SplitPane` node and returns them as a list of floats in range `[0.0, 1.0]`.

#### Scenario: Read single divider
- **WHEN** a script calls `get_divider_positions(id="demoSplitPane")` on a `SplitPane` with one divider
- **THEN** the action returns `ok: true` with `value` as a list containing one float

#### Scenario: Read multiple dividers
- **WHEN** a script calls `get_divider_positions(id="demoSplitPane")` on a `SplitPane` with two dividers
- **THEN** the action returns `ok: true` with `value` as a list of two floats in `[0.0, 1.0]`

### Requirement: Set divider position
The system SHALL support a `set_divider_position` action that sets the position of one divider in a `SplitPane` by index, accepting a float in `[0.0, 1.0]`.

#### Scenario: Set divider by index
- **WHEN** a script calls `set_divider_position(0, 0.3, id="demoSplitPane")`
- **THEN** the first divider of the `SplitPane` is moved to position `0.3` and the action returns `ok: true`

#### Scenario: Reject out-of-bounds index
- **WHEN** a script calls `set_divider_position(5, 0.5, id="demoSplitPane")` and the `SplitPane` has fewer than 6 dividers
- **THEN** the action returns `ok: false` with `reason: "invalid_divider_index"`
