## Purpose

Define demo scenarios for input and navigation controls in the reference application.

## Requirements

### Requirement: Input and navigation demo scenes
The demo application SHALL include dedicated demo sections for each new control
type, and corresponding Python demo scripts SHALL exercise the full automation
API for each control.

#### Scenario: RadioButton and ToggleButton demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `radioToggleSection` is visible containing
  at least two RadioButtons in a ToggleGroup and one ToggleButton

#### Scenario: Slider and Spinner demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `sliderSpinnerSection` is visible containing
  a Slider (`id="demoSlider"`) and a Spinner (`id="demoSpinner"`)

#### Scenario: ProgressBar demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `progressSection` is visible containing
  a ProgressBar (`id="demoProgressBar"`) and a ProgressIndicator
  (`id="demoProgressIndicator"`)

#### Scenario: TabPane demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `tabSection` is visible containing a TabPane
  (`id="demoTabPane"`) with at least three tabs

#### Scenario: Python demo scripts pass for all new controls
- **WHEN** each new Python demo script is run against the demo application
- **THEN** the script exits with code 0 and prints a success message