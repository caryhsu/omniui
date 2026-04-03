# javafx-automation-core (delta)

## Delta: add-text-input-controls

### Added supported node types for get_text / type actions

`TextArea` and `PasswordField` nodes are now explicitly supported for:
- `get_text` — calls `getText()` on the node
- `type` — calls `setText(String)` on the node

Both extend `TextInputControl`, so the existing `handleType` and `get_text` reflection
paths handle them without additional branching.

### Added supported node type for click action

`Hyperlink` nodes (which extend `ButtonBase`) are now supported for:
- `click` — calls `fire()` on the node via the existing `handleClick` path
