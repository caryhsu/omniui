## ADDED Requirements

### Requirement: Recorder GUI provides Run Selection button
The system SHALL provide a **Run Selection** button in the Recorder GUI toolbar. When clicked, the system SHALL execute only the text currently selected in the script preview widget against the connected `OmniUIClient` session. If no text is selected, the system SHALL display `❌ No text selected` in the status bar and SHALL NOT attempt execution.

#### Scenario: Run Selection executes the highlighted fragment
- **WHEN** the user selects one or more lines in the script preview and clicks **Run Selection**
- **THEN** only the selected text is passed to `exec(code, {"client": self.client})`
- **THEN** if execution completes without exception, the status bar shows `✅ Passed`

#### Scenario: Run Selection shows failure message on exception
- **WHEN** the selected fragment raises an exception during execution
- **THEN** the status bar shows `❌ Failed: <exception message>`

#### Scenario: Run Selection with no selection shows warning
- **WHEN** the user clicks **Run Selection** with no text selected in the preview
- **THEN** the status bar shows `❌ No text selected`
- **THEN** no execution is attempted
