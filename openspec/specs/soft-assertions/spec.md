## ADDED Requirements

### Requirement: Soft assert context manager collects failures
The system SHALL provide a `SoftAssertContext` that can be used as a context manager via `client.soft_assert()` or the module-level `soft_assert()`. Inside the block, `AssertionError` exceptions SHALL be caught and accumulated rather than raised immediately. All other exception types SHALL propagate immediately.

#### Scenario: Single failure is collected and re-raised at exit
- **WHEN** exactly one `AssertionError` is raised inside the `with soft_assert():` block
- **THEN** the block does not raise during execution
- **THEN** a single `AssertionError` is raised at block exit containing the original message

#### Scenario: Multiple failures are all collected and reported together
- **WHEN** two or more `AssertionError` exceptions are raised inside the block
- **THEN** none of them interrupt execution of subsequent statements
- **THEN** a single `AssertionError` is raised at block exit listing all failures

#### Scenario: No failures — block exits cleanly
- **WHEN** no `AssertionError` is raised inside the block
- **THEN** the block exits without raising any exception

#### Scenario: Non-assertion errors propagate immediately
- **WHEN** an exception that is NOT an `AssertionError` is raised inside the block
- **THEN** that exception propagates immediately and is not swallowed

### Requirement: Combined error message format
The combined `AssertionError` at block exit SHALL begin with `"N assertion(s) failed:"` (where N is the count) followed by each failure numbered and including its original message.

#### Scenario: Message lists all failures with index
- **WHEN** 3 failures are collected with messages "A", "B", "C"
- **THEN** the combined message reads `"3 assertion(s) failed:\n  1. A\n  2. B\n  3. C"`

### Requirement: Module-level soft_assert exported from omniui
The system SHALL export `soft_assert` as a module-level function from the `omniui` package so callers can use `omniui.soft_assert()` without a client instance.

#### Scenario: Module-level import works
- **WHEN** `from omniui import soft_assert` is executed
- **THEN** the import succeeds and `soft_assert()` returns a usable context manager

### Requirement: OmniUIClient.soft_assert delegates to module-level function
`OmniUIClient` SHALL expose a `soft_assert()` instance method that delegates to the module-level `soft_assert()`, allowing `client.soft_assert()` usage in test scripts.

#### Scenario: Client method behaves identically to module function
- **WHEN** `with client.soft_assert():` is used with a failing assertion inside
- **THEN** it behaves identically to `with soft_assert():`
