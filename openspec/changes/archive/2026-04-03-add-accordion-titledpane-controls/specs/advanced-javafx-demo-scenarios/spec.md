## ADDED Requirements

### Requirement: Accordion and TitledPane demo scene
The demo application SHALL include a dedicated demo section for Accordion and TitledPane controls, with a corresponding Python demo script that exercises expand, collapse, and state-read operations.

#### Scenario: Accordion demo section visible
- **WHEN** the demo application is running
- **THEN** a section with id `accordionSection` is visible containing an Accordion (`id="demoAccordion"`) with at least three TitledPanes with distinct `fx:id` values

#### Scenario: Python accordion demo script passes
- **WHEN** `accordion_demo.py` is run against the demo application
- **THEN** the script exits with code 0 and prints a success message
