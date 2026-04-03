## ADDED Requirements

### Requirement: Advanced JavaFX demo coverage
The system SHALL provide reference JavaFX demo scenarios that exercise advanced control patterns beyond the basic login flow, including `ComboBox`, `ListView`, `TreeView`, `TableView`, and grid-oriented layouts.

#### Scenario: Open advanced demo scenarios
- **WHEN** a user launches the reference JavaFX demo application
- **THEN** the application exposes identifiable scenarios for advanced controls rather than only the basic login flow

### Requirement: Stable demo data for advanced controls
The system SHALL use deterministic, human-readable sample data in advanced demo scenarios so structural discovery and automation assertions remain interpretable.

#### Scenario: Inspect advanced control dataset
- **WHEN** the user or Python client inspects advanced scenarios
- **THEN** visible options, rows, items, and tree labels are stable, uniquely named, and suitable for selector-based automation

### Requirement: Runnable advanced Python demos
The system SHALL provide runnable Python demos that exercise the advanced JavaFX scenarios and print enough output to inspect discovery and action behavior.

#### Scenario: Run advanced control demo script
- **WHEN** a user executes a Python demo for advanced JavaFX scenarios against an agent-enabled app
- **THEN** the script exercises representative control interactions and reports the resulting discovery or action trace
