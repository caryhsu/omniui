## ADDED Requirements

### Requirement: set_page jumps to a specific page index
The system SHALL provide `set_page(id, page)` that sets the current page to the given 0-based index. Values outside `[0, page_count-1]` SHALL be silently clamped.

#### Scenario: Set page within range
- **WHEN** `client.set_page(id="demoPagination", page=2)` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `get_page` returns `page=2`

#### Scenario: Set page out of range is clamped
- **WHEN** `client.set_page(id="demoPagination", page=999)` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `get_page` returns `page=page_count-1`

### Requirement: next_page and prev_page navigate one step
The system SHALL provide `next_page(id)` and `prev_page(id)` that advance or go back one page. At the boundary (first or last page) the call SHALL succeed and the index SHALL remain unchanged.

#### Scenario: next_page advances one page
- **WHEN** current page is 0 and `client.next_page(id="demoPagination")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `get_page` returns `page=1`

#### Scenario: prev_page goes back one page
- **WHEN** current page is 2 and `client.prev_page(id="demoPagination")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** `get_page` returns `page=1`

#### Scenario: next_page at last page is a no-op
- **WHEN** current page equals `page_count-1` and `client.next_page(id="demoPagination")` is called
- **THEN** `ActionResult.ok` is `True`
- **THEN** page index is unchanged
