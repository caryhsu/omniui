## ADDED Requirements

### Requirement: is_visible method
The Python client SHALL expose `is_visible(**selector) -> bool` as a public method, consistent in style with `is_visited()` and `get_expanded()`.

#### Scenario: is_visible returns bool
- Given the app is running
- When `is_visible(id="someNode")` is called
- Then the method returns a `bool` value

### Requirement: is_enabled method
The Python client SHALL expose `is_enabled(**selector) -> bool` as a public method, consistent in style with `is_visited()` and `get_expanded()`.

#### Scenario: is_enabled returns bool
- Given the app is running
- When `is_enabled(id="someNode")` is called
- Then the method returns a `bool` value
