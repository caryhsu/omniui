## ADDED Requirements

### Requirement: CheckBox and ChoiceBox demo scenes
The demo application SHALL include dedicated demo sections for CheckBox and ChoiceBox controls, and corresponding Python demo scripts SHALL exercise the full automation API for each control.

#### Scenario: CheckBox demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `checkBoxSection` is visible containing at least three independent CheckBoxes with distinct `fx:id` values

#### Scenario: ChoiceBox demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `choiceBoxSection` is visible containing a ChoiceBox (`id="demoChoiceBox"`) pre-populated with at least three selectable string items

#### Scenario: Python demo scripts pass for CheckBox and ChoiceBox
- **WHEN** `checkbox_demo.py` and `choicebox_demo.py` are run against the demo application
- **THEN** each script exits with code 0 and prints a success message
