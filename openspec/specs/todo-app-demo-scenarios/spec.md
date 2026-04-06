## ADDED Requirements

### Requirement: Add a task via dialog
The system SHALL allow adding a new task by clicking the **Add** button in the ToolBar. A dialog SHALL appear with fields for title (`dialogTitleField`), priority (`dialogPriorityCombo`), and due date (`dialogDatePicker`). Clicking OK SHALL add the task to the ListView.

#### Scenario: Add task with all fields
- **WHEN** user clicks `addButton`, fills in `dialogTitleField`, selects priority, sets due date, and clicks OK
- **THEN** the task appears in the ListView with correct title, priority, and due date

#### Scenario: Add button disabled when title is empty
- **WHEN** the dialog's title TextField (`dialogTitleField`) is empty
- **THEN** the dialog OK button SHALL be disabled

### Requirement: Edit a task via dialog
The system SHALL allow editing a task by clicking the **✎ edit** button (`edit_<idx>`) in the task's row. A dialog SHALL appear pre-filled with the task's current data. Clicking OK SHALL update the task in the ListView.

#### Scenario: Edit task title
- **WHEN** user clicks `edit_0`, changes the title in `dialogTitleField`, then clicks OK
- **THEN** the ListView item updates to show the new title

### Requirement: Delete a task with confirmation
The system SHALL allow deleting a task by clicking the **🗑 delete** button (`delete_<idx>`) in the task's row. A confirmation Alert SHALL appear before deletion.

#### Scenario: Delete selected task
- **WHEN** user clicks `delete_0` and confirms the Alert
- **THEN** the task is removed from the ListView

### Requirement: Mark task as completed via CheckBox
The system SHALL allow toggling a task's completion state via the CheckBox (`check_<idx>`) inside each ListView cell. Completed tasks SHALL be visually distinguished (strikethrough title).

#### Scenario: Check task complete
- **WHEN** user clicks the CheckBox (`check_0`) in a task's ListView cell
- **THEN** the task's `completed` state toggles and the title label gains strikethrough style

### Requirement: Filter by search text
The system SHALL filter the ListView in real time as the user types in the Search TextField (`searchField`). Only tasks whose title contains the search text (case-insensitive) SHALL be shown.

#### Scenario: Search filters list
- **WHEN** user types a partial title in `searchField`
- **THEN** the ListView shows only tasks whose title contains the typed text

#### Scenario: Clear search restores full list
- **WHEN** user clears `searchField`
- **THEN** all tasks (subject to Show Completed filter) are displayed

### Requirement: Toggle show-completed filter
The system SHALL hide completed tasks by default. A ToggleButton (`showCompleted`) SHALL control whether completed tasks are visible.

#### Scenario: Hide completed tasks by default
- **WHEN** the app starts
- **THEN** `showCompleted` ToggleButton is deselected and completed tasks are not shown in the ListView

#### Scenario: Show completed tasks when toggled on
- **WHEN** user clicks `showCompleted` ToggleButton (selected state)
- **THEN** completed tasks appear in the ListView

### Requirement: Clear all tasks
The system SHALL provide a **Clear All** button (`clearButton`) in the ToolBar that removes all tasks from the list and resets the search and show-completed state. Used to ensure a clean state before demo / test runs.
