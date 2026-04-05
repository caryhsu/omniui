## MODIFIED Requirements

### Requirement: node lookup restricts search to a subtree when scope is provided

When the selector JSON includes a `"scope"` field, the system SHALL first resolve the scope container node on the full scene graph, then search for the target node only within the container's descendants. Without a `"scope"` field, behavior is unchanged.

#### Scenario: target found in scoped subtree
- **WHEN** selector `{"id": "ok", "scope": {"id": "leftPanel"}}` is sent
- **THEN** the agent locates `leftPanel`, then finds `ok` within its subtree
- **THEN** `ActionResult.ok` is `True`

#### Scenario: target not found in scoped subtree but exists elsewhere
- **WHEN** selector `{"id": "ok", "scope": {"id": "rightPanel"}}` is sent and `ok` only exists in `leftPanel`
- **THEN** `ActionResult.ok` is `False` and `reason` is `"selector_not_found"`

#### Scenario: scope container not found
- **WHEN** `"scope": {"id": "nonexistent"}` is sent
- **THEN** `ActionResult.ok` is `False` and `reason` is `"scope_not_found"`

#### Scenario: no scope field — full scene graph search (unchanged)
- **WHEN** selector `{"id": "ok"}` is sent with no `"scope"` field
- **THEN** the agent searches the full scene graph as before
