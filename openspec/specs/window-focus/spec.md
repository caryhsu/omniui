# Spec: Window Focus

## Purpose
Allow automation clients to bring a specific JavaFX Stage to the foreground.

## Requirements

### Requirement: focus_window brings a Stage to foreground
The system SHALL provide `focus_window(title)` that calls `toFront()` on the Stage whose title matches `title`.

#### Scenario: Focus an existing window
- **WHEN** `client.focus_window(title="My App")` is called
- **THEN** `ActionResult.ok` is `True`

#### Scenario: Window not found
- **WHEN** no Stage has the given title
- **THEN** `ActionResult.ok` is `False` and `reason="window_not_found"`
