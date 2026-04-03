## MODIFIED Requirements

### Requirement: Python client advanced-control compatibility
The system SHALL allow Python automation scripts to exercise the supported advanced JavaFX demo scenarios without dropping down to backend-specific transport details.

#### Scenario: Run advanced scenario from Python
- **WHEN** a user runs a Python script against a supported advanced JavaFX demo scenario
- **THEN** the script can express the needed interaction through the OmniUI client API and inspect the resulting state through normal client methods

### Requirement: High-level action API
The system SHALL provide high-level Python methods sufficient to express the supported advanced JavaFX demo interactions, extending beyond `click` and `type` if required by the supported scenario set.

#### Scenario: Use selection-oriented action for advanced control
- **WHEN** a supported advanced JavaFX scenario requires an interaction that is better modeled as selection or expansion instead of a raw click
- **THEN** the Python client exposes a high-level action that represents that interaction without requiring the user to manipulate transport payloads directly
