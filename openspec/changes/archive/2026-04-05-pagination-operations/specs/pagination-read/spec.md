## ADDED Requirements

### Requirement: get_page returns current page index and total count
The system SHALL provide `get_page(id)` on `OmniUIClient` that returns the current page index and total page count of the `Pagination` node identified by `id`. The result SHALL include `page` (int, 0-based) and `page_count` (int).

#### Scenario: Query current page of a Pagination control
- **WHEN** `client.get_page(id="demoPagination")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `ActionResult.value` is a dict with keys `page` and `page_count`
- **THEN** `page` is within `[0, page_count - 1]`

#### Scenario: Pagination not found
- **WHEN** `id` does not match any node in the scene
- **THEN** `ActionResult.ok` is `False`
- **THEN** result contains `reason="selector_not_found"`

#### Scenario: Node is not a Pagination
- **WHEN** `id` matches a node that is not a `Pagination`
- **THEN** `ActionResult.ok` is `False`
- **THEN** result contains `reason="action_not_supported"`
