## ADDED Requirements

### Requirement: get_toolbar_items returns a list of item descriptors
The system SHALL provide `get_toolbar_items(id)` on `OmniUIClient` that returns the list of items inside the `ToolBar` identified by `id`. Each item SHALL be described as a dict with keys `fxId` (string or null), `text` (string), `type` (simple class name), and `disabled` (boolean).

#### Scenario: Query items from a toolbar with buttons
- **WHEN** `client.get_toolbar_items(id="mainToolbar")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ActionResult.value` is a list of dicts, one per item
- **THEN** each dict contains `fxId`, `text`, `type`, and `disabled` keys

#### Scenario: Separator items are included
- **WHEN** the toolbar contains a `Separator` node
- **THEN** the separator appears in the list with `type="Separator"` and empty `text`

#### Scenario: Disabled button is reflected
- **WHEN** a toolbar button has `disable=true`
- **THEN** its descriptor has `disabled=true`

#### Scenario: ToolBar not found
- **WHEN** `id` does not match any node in the scene
- **THEN** `ActionResult.ok` is `False` and the result contains `reason="selector_not_found"`
