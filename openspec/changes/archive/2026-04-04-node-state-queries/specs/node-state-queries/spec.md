## Purpose

Define the behavior of `is_visible` and `is_enabled` node state query methods in the OmniUI Python client.

## ADDED Requirements

### Requirement: Visible state query
The system SHALL provide an `is_visible(**selector) -> bool` method that returns `True` if the matched node's `visible` metadata field is `True`, and `False` otherwise (including when no node matches the selector).

#### Scenario: Node is visible
- **WHEN** a script calls `is_visible(id="loginButton")` and the node exists with `visible=True`
- **THEN** the method returns `True`

#### Scenario: Node is hidden
- **WHEN** a script calls `is_visible(id="loginButton")` and the node exists with `visible=False`
- **THEN** the method returns `False`

#### Scenario: Node not found
- **WHEN** a script calls `is_visible(id="nonExistentNode")`
- **THEN** the method returns `False` without raising an exception

### Requirement: Enabled state query
The system SHALL provide an `is_enabled(**selector) -> bool` method that returns `True` if the matched node's `enabled` metadata field is `True`, and `False` otherwise (including when no node matches the selector).

#### Scenario: Node is enabled
- **WHEN** a script calls `is_enabled(id="loginButton")` and the node exists with `enabled=True`
- **THEN** the method returns `True`

#### Scenario: Node is disabled
- **WHEN** a script calls `is_enabled(id="loginButton")` and the node exists with `enabled=False`
- **THEN** the method returns `False`

#### Scenario: Node not found
- **WHEN** a script calls `is_enabled(id="nonExistentNode")`
- **THEN** the method returns `False` without raising an exception
