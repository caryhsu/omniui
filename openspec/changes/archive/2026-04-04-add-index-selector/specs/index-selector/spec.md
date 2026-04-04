## ADDED Requirements

### Requirement: Selector supports index field for positional node selection
The system SHALL accept an optional `index` field (0-based integer) in any selector. When present, the agent SHALL apply all other selector criteria first, then return the Nth matching node. If `index` is absent, the first match (index 0) is returned, preserving existing behaviour.

#### Scenario: Select second Button by type
- **WHEN** an automation script calls `click(type="Button", index=1)`
- **THEN** the agent resolves to the second `Button` node in the scene tree (0-based) and performs the action

#### Scenario: Select third Label by type
- **WHEN** an automation script calls `get_text(type="Label", index=2)`
- **THEN** the agent resolves to the third `Label` node and returns its text

#### Scenario: Default index=0 preserves existing behaviour
- **WHEN** an automation script calls `click(type="Button")` with no `index` field
- **THEN** the agent behaves identically to `click(type="Button", index=0)` — the first match is returned

#### Scenario: index out of range returns selector_not_found
- **WHEN** an automation script calls `click(type="Button", index=99)` and fewer than 100 Button nodes exist
- **THEN** the agent returns a failure result with reason `selector_not_found`

### Requirement: index= works with all existing selector combinations
The system SHALL apply `index=` consistently regardless of which other selector fields are used (`id`, `text`, `type`, or combinations).

#### Scenario: index= with id selector
- **WHEN** an automation script calls `click(id="row", index=1)`
- **THEN** the agent picks the second node whose fxId equals `"row"`

#### Scenario: index= with text+type selector
- **WHEN** an automation script calls `click(text="OK", type="Button", index=0)`
- **THEN** the agent picks the first Button whose text is `"OK"` — same as omitting index
