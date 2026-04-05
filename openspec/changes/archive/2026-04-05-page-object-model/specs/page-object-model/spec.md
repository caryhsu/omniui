## ADDED Requirements

### Requirement: OmniPage base class provides client access
The system SHALL provide an `OmniPage` base class in `omniui` that accepts an `OmniUIClient` in its constructor and exposes it as `self.client`. Subclasses SHALL be able to call any client method via `self.client`.

#### Scenario: Subclass can call client methods
- **WHEN** a class inherits `OmniPage` and calls `self.client.click(id="btn")` in a method
- **THEN** the call delegates to the underlying `OmniUIClient` without error

#### Scenario: OmniPage is importable from omniui
- **WHEN** a test does `from omniui import OmniPage`
- **THEN** the import succeeds

### Requirement: OmniPage provides locator shorthand
The system SHALL expose a `locator(**selector)` method on `OmniPage` that delegates to `self.client.locator(**selector)`, returning a `Locator` instance.

#### Scenario: locator shorthand returns a Locator
- **WHEN** `page.locator(id="myBtn")` is called on an `OmniPage` subclass
- **THEN** it returns the same result as `client.locator(id="myBtn")`
