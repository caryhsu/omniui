## ADDED Requirements

### Requirement: Accordion and TitledPane control support
The system SHALL support `Accordion` and `TitledPane` as automation targets, with `expand_pane`, `collapse_pane`, and `get_expanded` actions routing to the `TitledPane`'s `setExpanded(boolean)` and `isExpanded()` methods.

#### Scenario: expand_pane action routes to setExpanded(true)
- **WHEN** `perform("expand_pane", selector, null)` is called on a TitledPane node
- **THEN** `titledPane.setExpanded(true)` is invoked on the FX thread and the action returns success

#### Scenario: collapse_pane action routes to setExpanded(false)
- **WHEN** `perform("collapse_pane", selector, null)` is called on a TitledPane node
- **THEN** `titledPane.setExpanded(false)` is invoked on the FX thread and the action returns success

#### Scenario: get_expanded action routes to isExpanded()
- **WHEN** `perform("get_expanded", selector, null)` is called on a TitledPane node
- **THEN** the action returns `ActionResult.success` with value equal to `titledPane.isExpanded()`
