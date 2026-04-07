## ADDED Requirements

### Requirement: Recorder GUI provides Run All button
The system SHALL provide a **Run All** button in the Recorder GUI toolbar. When clicked, the system SHALL execute the entire script text currently displayed in the script preview widget against the connected `OmniUIClient` session using `exec()`. Execution SHALL run in a background thread so the GUI remains responsive. The Run All and Run Selection buttons SHALL be disabled while a run is in progress. On completion the system SHALL display a status message indicating pass or fail.

#### Scenario: Run All executes the full script and shows Passed
- **WHEN** the user clicks **Run All** after recording
- **THEN** the full script text is passed to `exec(code, {"client": self.client})`
- **THEN** if execution completes without exception, the status bar shows `✅ Passed`

#### Scenario: Run All shows failure message on exception
- **WHEN** execution raises an exception
- **THEN** the status bar shows `❌ Failed: <exception message>`
- **THEN** the Run All and Run Selection buttons are re-enabled

#### Scenario: Run buttons disabled during execution
- **WHEN** a run is in progress
- **THEN** both Run All and Run Selection buttons are in disabled state
- **THEN** buttons are re-enabled once the run completes (pass or fail)
