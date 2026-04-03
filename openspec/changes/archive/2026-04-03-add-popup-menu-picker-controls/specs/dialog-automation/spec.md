## ADDED Requirements

### Requirement: Dialog detection
The system SHALL detect an open JavaFX Dialog by identifying a visible Stage whose scene root is a `DialogPane`, and SHALL return a descriptor containing the dialog's title, header text when present, content text, and list of available button labels.

#### Scenario: Detect an open modal Dialog
- **WHEN** a JavaFX Dialog is shown and the Python client calls `get_dialog()`
- **THEN** the system returns a dialog descriptor containing the dialog's title, header text when present, content text, and available button labels

#### Scenario: Return empty result when no Dialog is open
- **WHEN** no JavaFX Dialog is visible and the Python client calls `get_dialog()`
- **THEN** the system returns an empty result without raising an error

### Requirement: Dialog content reading
The system SHALL read the title, header text, and content text of an open Dialog from the `DialogPane` node tree.

#### Scenario: Read Dialog title and content text
- **WHEN** a modal Dialog is open with title "Confirm Action" and content text "Are you sure you want to delete this file?"
- **THEN** calling `get_dialog()` returns a descriptor where `title` is "Confirm Action" and `content_text` is "Are you sure you want to delete this file?"

#### Scenario: Read Dialog with header text
- **WHEN** a modal Dialog is open with header text "Unsaved Changes" and content text "Save before closing?"
- **THEN** calling `get_dialog()` returns a descriptor where `header_text` is "Unsaved Changes" and `content_text` is "Save before closing?"

### Requirement: Dialog button interaction
The system SHALL support clicking a named button in an open Dialog by matching its label text, with `ButtonData` semantic type as an alternative matching strategy.

#### Scenario: Click a Dialog button by label text
- **WHEN** a modal Dialog is open and the Python client calls `dismiss_dialog(button="OK")`
- **THEN** the system locates the button with label "OK" in the `DialogPane`'s `ButtonBar` and dispatches a click event on it

#### Scenario: Click a Dialog button by ButtonData type
- **WHEN** a modal Dialog is open and the Python client calls `dismiss_dialog(button_type="CANCEL_CLOSE")`
- **THEN** the system locates the button whose `ButtonData` matches `CANCEL_CLOSE` and dispatches a click event on it

#### Scenario: Report error when button is not found
- **WHEN** a modal Dialog is open and the Python client calls `dismiss_dialog(button="Apply")` but no "Apply" button exists
- **THEN** the system reports an error listing the available button labels without dismissing the Dialog
