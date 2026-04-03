## ADDED Requirements

### Requirement: DatePicker popup open
The system SHALL support opening a DatePicker's calendar popup by dispatching a click on the DatePicker's calendar toggle button.

#### Scenario: Open DatePicker popup via selector
- **WHEN** the Python client calls `open_datepicker(selector)` targeting a DatePicker node
- **THEN** the system dispatches a click on the DatePicker's calendar toggle button and waits until the calendar popup window is visible before returning

### Requirement: DatePicker date selection
The system SHALL support selecting a specific date in an open DatePicker popup by matching the target date cell using its `LocalDate` item property value.

#### Scenario: Select a date visible in the current month
- **WHEN** the DatePicker popup is open, the target date is in the currently displayed month, and the Python client calls `pick_date(date="2025-03-15")`
- **THEN** the system locates the date cell whose `LocalDate` item matches March 15 2025 and dispatches a click event on it

#### Scenario: Select a date requiring forward month navigation
- **WHEN** the DatePicker popup is open, the target date is in a future month not currently displayed, and the Python client calls `pick_date(date="2025-06-20")`
- **THEN** the system navigates forward by clicking the next-month button as many times as needed to reach June 2025, then dispatches a click on the cell matching June 20 2025

#### Scenario: Select a date requiring backward month navigation
- **WHEN** the DatePicker popup is open, the target date is in a past month not currently displayed, and the Python client calls `pick_date(date="2025-01-05")`
- **THEN** the system navigates backward by clicking the previous-month button as many times as needed to reach January 2025, then dispatches a click on the cell matching January 5 2025

### Requirement: DatePicker month navigation
The system SHALL support navigating the DatePicker calendar forward or backward one month at a time by dispatching a click on the navigation buttons.

#### Scenario: Navigate to the next month
- **WHEN** the DatePicker popup is open and the Python client calls `navigate_month(direction="forward")`
- **THEN** the system dispatches a click on the next-month navigation button and waits for the calendar grid to update to the following month

#### Scenario: Navigate to the previous month
- **WHEN** the DatePicker popup is open and the Python client calls `navigate_month(direction="backward")`
- **THEN** the system dispatches a click on the previous-month navigation button and waits for the calendar grid to update to the preceding month
