## Purpose

Define the `get_style` and `get_style_class` actions that allow automation scripts to inspect the CSS styling state of JavaFX nodes — enabling validation state testing via color, border, or CSS class without depending on specific label text.

## Requirements

### Requirement: get_style returns a node's inline CSS style string
The system SHALL provide a `get_style(**selector)` action that resolves a node and returns the value of `node.getStyle()` — the inline style string set via `setStyle()`. If no inline style is set the system SHALL return `""`. If the node cannot be resolved the system SHALL return a `selector_not_found` failure.

#### Scenario: Node has an inline style
- **WHEN** an automation script calls `get_style(id="validationLabel")` and that node has inline style `"-fx-text-fill: red;"`
- **THEN** the action returns `ok=True` with `value = "-fx-text-fill: red;"`

#### Scenario: Node has no inline style
- **WHEN** an automation script calls `get_style(id="loginButton")` and no `setStyle()` has been called on that node
- **THEN** the action returns `ok=True` with `value = ""`

#### Scenario: Node not found
- **WHEN** an automation script calls `get_style(id="nonexistent")`
- **THEN** the action returns `ok=False` with reason `selector_not_found`

### Requirement: get_style_class returns a node's CSS class list
The system SHALL provide a `get_style_class(**selector)` action that resolves a node and returns the list of CSS class names from `node.getStyleClass()` as a JSON array of strings. If the node cannot be resolved the system SHALL return a `selector_not_found` failure.

#### Scenario: Node has CSS classes
- **WHEN** an automation script calls `get_style_class(id="errorField")` and that node has style classes `["text-field", "error"]`
- **THEN** the action returns `ok=True` with `value = ["text-field", "error"]`

#### Scenario: Node has default CSS classes
- **WHEN** an automation script calls `get_style_class(id="loginButton")` and the node only has default classes
- **THEN** the action returns `ok=True` with `value` being a list containing the default JavaFX CSS class names (e.g. `["button"]`)

#### Scenario: Node not found
- **WHEN** an automation script calls `get_style_class(id="nonexistent")`
- **THEN** the action returns `ok=False` with reason `selector_not_found`

### Requirement: get_style only reflects inline styles, not stylesheet-applied styles
The system SHALL document that `get_style()` returns only the inline style set via `node.setStyle()`. Styles applied through external CSS files or scene stylesheets are NOT returned.

#### Scenario: Stylesheet-applied style not visible via get_style
- **WHEN** a node's color is set only through an external CSS stylesheet (not via `setStyle()`)
- **THEN** `get_style()` returns `""` for that node
