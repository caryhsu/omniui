## ADDED Requirements

### Requirement: Alert detection and type identification
The system SHALL detect an open JavaFX Alert and identify its `AlertType`, returning a descriptor that includes the alert type, title, header text when present, content text, and available button labels.

#### Scenario: Detect an open CONFIRMATION Alert
- **WHEN** a JavaFX Alert of type CONFIRMATION is shown and the Python client calls `get_dialog()`
- **THEN** the system returns a descriptor that includes `alert_type` set to "CONFIRMATION", along with title and content text

#### Scenario: Detect an ERROR Alert
- **WHEN** a JavaFX Alert of type ERROR is shown and the Python client calls `get_dialog()`
- **THEN** the system returns a descriptor with `alert_type` set to "ERROR" and the alert's content text

#### Scenario: Detect an INFORMATION Alert
- **WHEN** a JavaFX Alert of type INFORMATION is shown and the Python client calls `get_dialog()`
- **THEN** the system returns a descriptor with `alert_type` set to "INFORMATION" and the alert's content text

#### Scenario: Detect a WARNING Alert
- **WHEN** a JavaFX Alert of type WARNING is shown and the Python client calls `get_dialog()`
- **THEN** the system returns a descriptor with `alert_type` set to "WARNING" and the alert's content text

### Requirement: Alert message reading
The system SHALL read the header text and content text of an open Alert.

#### Scenario: Read Alert content text
- **WHEN** an Alert is open with content text "File not found"
- **THEN** calling `get_dialog()` returns a descriptor where `content_text` is "File not found"

#### Scenario: Read Alert with both header text and content text
- **WHEN** an Alert is open with header text "Save failed" and content text "The disk is full"
- **THEN** calling `get_dialog()` returns a descriptor with `header_text` set to "Save failed" and `content_text` set to "The disk is full"

### Requirement: Alert button interaction
The system SHALL support clicking a named button in an open Alert by matching its label text, with `ButtonData` semantic type as an alternative matching strategy.

#### Scenario: Click the OK button in an INFORMATION Alert
- **WHEN** an INFORMATION Alert is open and the Python client calls `dismiss_dialog(button="OK")`
- **THEN** the system locates and dispatches a click on the OK button in the Alert's `ButtonBar`

#### Scenario: Click Cancel in a CONFIRMATION Alert
- **WHEN** a CONFIRMATION Alert with OK and Cancel buttons is open and the Python client calls `dismiss_dialog(button="Cancel")`
- **THEN** the system locates and dispatches a click on the Cancel button, and the Alert is dismissed

#### Scenario: Click a button by ButtonData type in an Alert
- **WHEN** a CONFIRMATION Alert is open and the Python client calls `dismiss_dialog(button_type="OK_DONE")`
- **THEN** the system locates the button whose `ButtonData` matches `OK_DONE` and dispatches a click event on it
