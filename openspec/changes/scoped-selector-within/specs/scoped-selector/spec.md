## ADDED Requirements

### Requirement: within context manager scopes actions to a container

The system SHALL provide `client.within(**selector)` as a context manager on `OmniUIClient`. While inside the `with` block, every action that performs node lookup SHALL restrict its search to the subtree rooted at the matched container node.

#### Scenario: click inside scoped container
- **WHEN** `with client.within(id="leftPanel"):` is entered and `client.click(id="ok")` is called
- **THEN** the agent resolves `ok` only within descendants of `leftPanel`
- **THEN** `ActionResult.ok` is `True`

#### Scenario: scope is cleared after context exits
- **WHEN** the `with client.within(...)` block exits
- **THEN** subsequent actions search the full scene graph as normal

#### Scenario: scope container not found
- **WHEN** `with client.within(id="missing"):` is entered and an action is called
- **THEN** `ActionResult.ok` is `False` and `details["reason"]` is `"scope_not_found"`

#### Scenario: multiple actions inside same scope
- **WHEN** two or more actions are called inside the same `with client.within(...)` block
- **THEN** all actions are scoped to the same container without re-specifying the container
