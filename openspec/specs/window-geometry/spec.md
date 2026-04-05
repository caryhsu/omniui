# Spec: Window Geometry

## Purpose
Allow automation clients to read and set the size and position of JavaFX Stage windows.

## Requirements

### Requirement: set_window_size resizes a Stage
The system SHALL provide `set_window_size(width, height, title)` that calls `setWidth(width)` and `setHeight(height)` on the matching Stage.

#### Scenario: Resize a window
- **WHEN** `client.set_window_size(width=800, height=600, title="My App")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `get_window_size` returns width≈800, height≈600

### Requirement: set_window_position moves a Stage
The system SHALL provide `set_window_position(x, y, title)` that calls `setX(x)` and `setY(y)`.

#### Scenario: Move a window
- **WHEN** `client.set_window_position(x=100, y=100, title="My App")` is called
- **THEN** `ActionResult.ok` is `True`

### Requirement: get_window_size returns current Stage dimensions
The system SHALL provide `get_window_size(title)` returning a dict with keys `width` and `height` (float).

#### Scenario: Read window size
- **WHEN** `client.get_window_size(title="My App")` is called
- **THEN** `ActionResult.value` is a dict with `width` and `height` keys

### Requirement: get_window_position returns current Stage position
The system SHALL provide `get_window_position(title)` returning a dict with keys `x` and `y` (float).

#### Scenario: Read window position
- **WHEN** `client.get_window_position(title="My App")` is called
- **THEN** `ActionResult.value` is a dict with `x` and `y` keys
