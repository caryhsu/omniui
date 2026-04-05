## ADDED Requirements

### Requirement: maximize_window maximizes a Stage
The system SHALL provide `maximize_window(title)` that calls `setMaximized(true)` on the matching Stage.

#### Scenario: Maximize a window
- **WHEN** `client.maximize_window(title="My App")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `is_maximized(title="My App")` returns `True`

### Requirement: minimize_window minimizes a Stage
The system SHALL provide `minimize_window(title)` that calls `setIconified(true)`.

#### Scenario: Minimize a window
- **WHEN** `client.minimize_window(title="My App")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `is_minimized(title="My App")` returns `True`

### Requirement: restore_window restores a Stage
The system SHALL provide `restore_window(title)` that sets both `setMaximized(false)` and `setIconified(false)`.

#### Scenario: Restore a maximized window
- **WHEN** `client.restore_window(title="My App")` is called after maximize
- **THEN** `ActionResult.ok` is `True`
- **THEN** `is_maximized` returns `False`

### Requirement: is_maximized and is_minimized query window state
The system SHALL provide `is_maximized(title)` and `is_minimized(title)` returning a boolean.

#### Scenario: Query maximized state
- **WHEN** `client.is_maximized(title="My App")` is called
- **THEN** `ActionResult.value` is `True` or `False` reflecting current state
